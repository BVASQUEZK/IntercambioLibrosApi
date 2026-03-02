package com.bardales.intercambiolibrosapi.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bardales.intercambiolibrosapi.dto.SolicitudCrearDTO;
import com.bardales.intercambiolibrosapi.dto.SolicitudResumenDTO;
import com.bardales.intercambiolibrosapi.exception.ForbiddenException;
import com.bardales.intercambiolibrosapi.exception.ResourceNotFoundException;
import com.bardales.intercambiolibrosapi.exception.UnauthorizedException;
import com.bardales.intercambiolibrosapi.repository.SolicitudRepository;
import com.bardales.intercambiolibrosapi.repository.SolicitudResumenProjection;
import com.bardales.intercambiolibrosapi.service.SolicitudService;

@Service
public class SolicitudServiceImpl implements SolicitudService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolicitudServiceImpl.class);

    private final SolicitudRepository solicitudRepository;
    private final JdbcTemplate jdbcTemplate;

    public SolicitudServiceImpl(SolicitudRepository solicitudRepository, JdbcTemplate jdbcTemplate) {
        this.solicitudRepository = solicitudRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public Integer registrarSolicitud(SolicitudCrearDTO dto) {
        Integer idSolicitud = jdbcTemplate.queryForObject(
                "INSERT INTO solicitud (id_solicitante, id_receptor, tipo, estado) VALUES (?, ?, ?, 'pendiente') RETURNING id_solicitud",
                Integer.class,
                dto.getIdSolicitante(),
                dto.getIdReceptor(),
                dto.getTipo());

        if (dto.getIdLibro() != null) {
            solicitudRepository.vincularDetalle(idSolicitud, dto.getIdLibro(), "receptor");
        }

        return idSolicitud;
    }

    @Override
    public List<SolicitudResumenDTO> listarPorUsuario(int idUsuario) {
        return solicitudRepository.listarPorUsuario(idUsuario)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void actualizarEstado(int idSolicitud, String nuevoEstado, int idUsuario) {
        Map<String, Object> solicitud;
        try {
            solicitud = jdbcTemplate.queryForMap(
                    "SELECT id_solicitante, id_receptor, estado FROM solicitud WHERE id_solicitud = ?",
                    idSolicitud);
        } catch (EmptyResultDataAccessException ex) {
            throw new ResourceNotFoundException("Solicitud no encontrada");
        }

        int idSolicitante = ((Number) solicitud.get("id_solicitante")).intValue();
        int idReceptor = ((Number) solicitud.get("id_receptor")).intValue();
        String estadoActual = String.valueOf(solicitud.get("estado")).toLowerCase();

        boolean esParticipante = idUsuario == idSolicitante || idUsuario == idReceptor;
        if (!esParticipante) {
            throw new ForbiddenException("No autorizado para actualizar esta solicitud");
        }

        if ("aceptado".equals(nuevoEstado) || "rechazado".equals(nuevoEstado)) {
            if (idUsuario != idReceptor) {
                throw new ForbiddenException("Solo el receptor puede aceptar o rechazar");
            }
            if (!"pendiente".equals(estadoActual)) {
                throw new UnauthorizedException("Solo se puede responder una solicitud pendiente");
            }
        } else if ("finalizado".equals(nuevoEstado)) {
            if (idUsuario != idReceptor) {
                throw new ForbiddenException("Solo el dueño del libro puede finalizar");
            }
            if (!"aceptado".equals(estadoActual)) {
                throw new UnauthorizedException("Solo se puede finalizar una solicitud aceptada");
            }
        } else if ("cancelado".equals(nuevoEstado)) {
            if (!"pendiente".equals(estadoActual) && !"aceptado".equals(estadoActual)) {
                throw new UnauthorizedException("No se puede cancelar una solicitud cerrada");
            }
        } else if ("pendiente".equals(nuevoEstado)) {
            throw new UnauthorizedException("No se permite volver al estado pendiente");
        }

        solicitudRepository.actualizarEstado(idSolicitud, nuevoEstado);

        if ("aceptado".equals(nuevoEstado)) {
            marcarLibrosComoOcupados(idSolicitud);
        }

        if ("finalizado".equals(nuevoEstado)) {
            liberarLibrosDeSolicitud(idSolicitud);
            actualizarEncuentroRealizado(idSolicitud);
        }

        if ("cancelado".equals(nuevoEstado) && "aceptado".equals(estadoActual)) {
            liberarLibrosDeSolicitud(idSolicitud);
        }
    }

    private void marcarLibrosComoOcupados(int idSolicitud) {
        jdbcTemplate.update(
                "UPDATE libro l SET disponible = FALSE, situacion = 'ocupado' "
                        + "FROM detalle_solicitud ds "
                        + "WHERE ds.id_solicitud = ? AND ds.id_libro = l.id_libro",
                idSolicitud);
    }

    private void liberarLibrosDeSolicitud(int idSolicitud) {
        jdbcTemplate.update(
                "UPDATE libro l SET disponible = TRUE, situacion = 'disponible' "
                        + "FROM detalle_solicitud ds "
                        + "WHERE ds.id_solicitud = ? AND ds.id_libro = l.id_libro "
                        + "AND COALESCE(l.estado, 'activo') = 'activo'",
                idSolicitud);
    }

    private void actualizarEncuentroRealizado(int idSolicitud) {
        if (!existeTabla("encuentro")) {
            return;
        }
        try {
            jdbcTemplate.update(
                    "UPDATE encuentro SET estado = 'realizado' WHERE id_solicitud = ? AND estado <> 'cancelado'",
                    idSolicitud);
        } catch (DataAccessException ex) {
            LOGGER.warn("No se pudo actualizar encuentro para solicitud {}: {}", idSolicitud, ex.getMessage());
        }
    }

    private boolean existeTabla(String tableName) {
        try {
            Integer total = jdbcTemplate.queryForObject(
                    "SELECT COUNT(1) FROM information_schema.tables "
                            + "WHERE table_schema = current_schema() AND table_name = ?",
                    Integer.class,
                    tableName);
            return total != null && total > 0;
        } catch (DataAccessException ex) {
            LOGGER.warn("No se pudo validar existencia de tabla {}: {}", tableName, ex.getMessage());
            return false;
        }
    }

    private SolicitudResumenDTO toDto(SolicitudResumenProjection p) {
        return new SolicitudResumenDTO(
                p.getId_solicitud(),
                p.getId_solicitante(),
                p.getId_receptor(),
                p.getId_libro(),
                p.getTitulo_libro(),
                p.getNombre_contraparte(),
                p.getTipo(),
                p.getEstado(),
                p.getFecha_solicitud(),
                p.getUrl_imagen()
        );
    }
}
