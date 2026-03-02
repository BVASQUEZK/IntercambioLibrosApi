package com.bardales.intercambiolibrosapi.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bardales.intercambiolibrosapi.dto.IntercambioMensajeDTO;
import com.bardales.intercambiolibrosapi.dto.IntercambioRespuestaDTO;
import com.bardales.intercambiolibrosapi.dto.IntercambioSolicitudDTO;
import com.bardales.intercambiolibrosapi.dto.IntercambioSolicitudResumenDTO;
import com.bardales.intercambiolibrosapi.entity.Libro;
import com.bardales.intercambiolibrosapi.exception.ForbiddenException;
import com.bardales.intercambiolibrosapi.exception.ResourceNotFoundException;
import com.bardales.intercambiolibrosapi.exception.UnauthorizedException;
import com.bardales.intercambiolibrosapi.repository.IntercambioJpaRepository;
import com.bardales.intercambiolibrosapi.repository.IntercambioMensajeProjection;
import com.bardales.intercambiolibrosapi.repository.IntercambioSolicitudProjection;
import com.bardales.intercambiolibrosapi.repository.LibroRepository;
import com.bardales.intercambiolibrosapi.repository.UsuarioRepository;
import com.bardales.intercambiolibrosapi.service.IntercambioService;

@Service
public class IntercambioServiceImpl implements IntercambioService {

    private final IntercambioJpaRepository intercambioRepository;
    private final LibroRepository libroRepository;
    private final UsuarioRepository usuarioRepository;
    private final JdbcTemplate jdbcTemplate;

    public IntercambioServiceImpl(IntercambioJpaRepository intercambioRepository, LibroRepository libroRepository,
            UsuarioRepository usuarioRepository, JdbcTemplate jdbcTemplate) {
        this.intercambioRepository = intercambioRepository;
        this.libroRepository = libroRepository;
        this.usuarioRepository = usuarioRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public Integer enviarSolicitud(int idUsuarioSolicitante, IntercambioSolicitudDTO dto) {
        if (idUsuarioSolicitante <= 0 || !usuarioRepository.existsById(idUsuarioSolicitante)) {
            throw new UnauthorizedException("Sesion invalida. Vuelve a iniciar sesion");
        }

        Libro libro = libroRepository.findById(dto.getIdLibroInteresado())
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado"));
        Integer idReceptor = libro.getIdUsuario();
        if (idReceptor == null || idReceptor <= 0 || !usuarioRepository.existsById(idReceptor)) {
            throw new ResourceNotFoundException("No se encontro al propietario del libro");
        }

        if (idReceptor.equals(idUsuarioSolicitante)) {
            throw new UnauthorizedException("No puedes solicitar tu propio libro");
        }

        String situacion = libro.getSituacion() == null ? "" : libro.getSituacion().trim().toLowerCase();
        boolean ocupadoPorSituacion = "ocupado".equals(situacion);
        boolean ocupadoPorDisponible = libro.getDisponible() != null && !libro.getDisponible();
        if (ocupadoPorSituacion || ocupadoPorDisponible) {
            throw new ForbiddenException("El libro esta ocupado");
        }

        Optional<Integer> solicitudExistente = buscarSolicitudActiva(
                idUsuarioSolicitante,
                idReceptor,
                dto.getIdLibroInteresado());
        Integer idSolicitud = solicitudExistente.orElseGet(() -> crearSolicitudSegura(
                idUsuarioSolicitante,
                idReceptor,
                dto.getIdLibroInteresado()));

        if (idSolicitud == null || idSolicitud <= 0) {
            throw new RuntimeException("No se pudo crear la solicitud de intercambio");
        }

        if (solicitudExistente.isEmpty()) {
            jdbcTemplate.update(
                    "INSERT INTO detalle_solicitud (id_solicitud, id_libro, propietario) VALUES (?, ?, 'receptor')",
                    idSolicitud, dto.getIdLibroInteresado());
        }

        if (dto.getMensajePropuesta() != null && !dto.getMensajePropuesta().isBlank()) {
            jdbcTemplate.update(
                    "INSERT INTO mensaje (id_solicitud, id_emisor, contenido) VALUES (?, ?, ?)",
                    idSolicitud, idUsuarioSolicitante, dto.getMensajePropuesta().trim());
        }

        return idSolicitud;
    }

    private Integer crearSolicitudSegura(int idSolicitante, int idReceptor, int idLibroReceptor) {
        try {
            return jdbcTemplate.queryForObject(
                    "INSERT INTO solicitud (id_solicitante, id_receptor, tipo, estado) VALUES (?, ?, 'intercambio', 'pendiente') RETURNING id_solicitud",
                    Integer.class,
                    idSolicitante,
                    idReceptor);
        } catch (DataIntegrityViolationException ex) {
            if (!usuarioRepository.existsById(idSolicitante)) {
                throw new UnauthorizedException("Sesion invalida. Vuelve a iniciar sesion");
            }
            if (!usuarioRepository.existsById(idReceptor)) {
                throw new ResourceNotFoundException("No se encontro al propietario del libro");
            }
            throw ex;
        } catch (DataAccessException ex) {
            Optional<Integer> existente = buscarSolicitudActiva(idSolicitante, idReceptor, idLibroReceptor);
            if (existente.isPresent()) {
                return existente.get();
            }
            throw ex;
        }
    }

    private Optional<Integer> buscarSolicitudActiva(int idSolicitante, int idReceptor, int idLibroReceptor) {
        List<Integer> ids = jdbcTemplate.query(
                "SELECT s.id_solicitud "
                        + "FROM solicitud s "
                        + "INNER JOIN detalle_solicitud ds ON ds.id_solicitud = s.id_solicitud "
                        + "WHERE s.id_solicitante = ? AND s.id_receptor = ? "
                        + "AND ds.id_libro = ? AND ds.propietario = 'receptor' "
                        + "AND s.estado IN ('pendiente', 'aceptado') "
                        + "ORDER BY s.fecha_solicitud DESC LIMIT 1",
                (rs, rowNum) -> rs.getInt(1),
                idSolicitante, idReceptor, idLibroReceptor);
        if (ids.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(ids.get(0));
    }

    @Override
    @Transactional
    public void responderSolicitud(int idUsuario, int idSolicitud, IntercambioRespuestaDTO dto) {
        Integer idDueno = intercambioRepository.obtenerIdDuenoLibro(idSolicitud);
        if (idDueno == null) {
            throw new ResourceNotFoundException("Solicitud no encontrada");
        }
        if (!idDueno.equals(idUsuario)) {
            throw new UnauthorizedException("No autorizado para responder esta solicitud");
        }

        String estado = dto.getNuevoEstado() == null ? "" : dto.getNuevoEstado().trim().toUpperCase();
        if (!"ACEPTADA".equals(estado) && !"RECHAZADA".equals(estado)) {
            throw new UnauthorizedException("Estado inválido");
        }

        String estadoDb = "ACEPTADA".equals(estado) ? "aceptado" : "rechazado";

        jdbcTemplate.update(
                "UPDATE solicitud SET estado = ? WHERE id_solicitud = ?",
                estadoDb, idSolicitud);

        if ("aceptado".equals(estadoDb)) {
            jdbcTemplate.update(
                    "UPDATE libro l SET disponible = FALSE "
                            +
                            "FROM detalle_solicitud ds "
                            +
                            "WHERE ds.id_solicitud = ? AND ds.propietario = 'receptor' AND ds.id_libro = l.id_libro",
                    idSolicitud);
        }

        if (dto.getComentario() != null && !dto.getComentario().isBlank()) {
            Integer idReceptor = jdbcTemplate.queryForObject(
                    "SELECT id_receptor FROM solicitud WHERE id_solicitud = ?",
                    Integer.class,
                    idSolicitud);
            if (idReceptor != null) {
                jdbcTemplate.update(
                        "INSERT INTO mensaje (id_solicitud, id_emisor, contenido) VALUES (?, ?, ?)",
                        idSolicitud, idReceptor, dto.getComentario().trim());
            }
        }
    }

    @Override
    public List<IntercambioSolicitudResumenDTO> listarSolicitudes(int idUsuario, String tipo) {
        String tipoNormalizado = tipo == null ? "" : tipo.trim().toUpperCase();
        if (!"RECIBIDAS".equals(tipoNormalizado) && !"ENVIADAS".equals(tipoNormalizado)) {
            throw new UnauthorizedException("Tipo inválido");
        }

        return intercambioRepository.listarSolicitudes(idUsuario, tipoNormalizado)
                .stream()
                .map(this::toResumenDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<IntercambioMensajeDTO> obtenerHistorial(int idUsuario, int idSolicitud) {
        Integer existe = intercambioRepository.existeUsuarioEnSolicitud(idSolicitud, idUsuario);
        if (existe == null || existe == 0) {
            throw new UnauthorizedException("No autorizado para ver este intercambio");
        }

        return intercambioRepository.listarMensajesIntercambio(idSolicitud, idUsuario)
                .stream()
                .map(this::toMensajeDto)
                .collect(Collectors.toList());
    }

    private IntercambioSolicitudResumenDTO toResumenDto(IntercambioSolicitudProjection p) {
        return new IntercambioSolicitudResumenDTO(
                p.getId_solicitud(),
                p.getTitulo_libro(),
                p.getNombre_otra_parte(),
                p.getEstado(),
                p.getFecha_solicitud()
        );
    }

    private IntercambioMensajeDTO toMensajeDto(IntercambioMensajeProjection p) {
        boolean esMio = p.getEs_mio() != null && p.getEs_mio();
        return new IntercambioMensajeDTO(
                p.getEmisor_nombre(),
                p.getContenido_mensaje(),
                p.getFecha_envio(),
                esMio
        );
    }
}
