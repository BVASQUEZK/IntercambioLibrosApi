package com.bardales.intercambiolibrosapi.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bardales.intercambiolibrosapi.dto.LibroCreadoDTO;
import com.bardales.intercambiolibrosapi.dto.LibroDTO;
import com.bardales.intercambiolibrosapi.dto.LibroHomeDTO;
import com.bardales.intercambiolibrosapi.dto.LibroRegistroDTO;
import com.bardales.intercambiolibrosapi.repository.LibroHomeProjection;
import com.bardales.intercambiolibrosapi.repository.LibroRepository;
import com.bardales.intercambiolibrosapi.service.LibroService;

@Service
public class LibroServiceImpl implements LibroService {

    private final LibroRepository libroRepository;
    private final JdbcTemplate jdbcTemplate;

    public LibroServiceImpl(LibroRepository libroRepository, JdbcTemplate jdbcTemplate) {
        this.libroRepository = libroRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<LibroHomeDTO> obtenerLibrosRecientes(int pagina, int cantidad) {
        int safePagina = Math.max(pagina, 1);
        int safeCantidad = Math.max(cantidad, 1);
        int offset = (safePagina - 1) * safeCantidad;

        return libroRepository.listarRecientes(safeCantidad, offset)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LibroDTO> buscarLibros(String query, Integer idCategoria, String estado, int pagina, int cantidad) {
        int safePagina = Math.max(pagina, 1);
        int safeCantidad = Math.max(cantidad, 1);
        int offset = (safePagina - 1) * safeCantidad;
        String filtro = (query == null || query.isBlank()) ? null : query.trim();
        String estadoFiltro = (estado == null || estado.isBlank()) ? null : estado.trim();

        return libroRepository.buscarLibros(filtro, filtro, idCategoria, estadoFiltro, safeCantidad, offset)
                .stream()
                .map(this::toBusquedaDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LibroCreadoDTO registrarLibro(int idUsuario, LibroRegistroDTO dto) {
        String estadoNormalizado = normalizarEstado(dto.getEstado());

        Integer idLibro = jdbcTemplate.queryForObject(
                "INSERT INTO libro (id_usuario, id_categoria, titulo, autor, descripcion, estado, ubicacion, disponible) "
                        +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, TRUE) RETURNING id_libro",
                Integer.class,
                idUsuario,
                dto.getIdCategoria(),
                dto.getTitulo(),
                dto.getAutor(),
                dto.getDescripcion(),
                estadoNormalizado,
                dto.getUbicacion());

        if (idLibro == null || idLibro <= 0) {
            throw new RuntimeException("No se pudo registrar el libro");
        }

        if (dto.getUrlsImagenes() != null) {
            for (String url : dto.getUrlsImagenes()) {
                if (url == null || url.isBlank()) {
                    continue;
                }
                libroRepository.vincularImagen(idLibro, url.trim());
            }
        }

        return new LibroCreadoDTO(
                idLibro,
                idUsuario,
                dto.getIdCategoria(),
                dto.getTitulo(),
                dto.getAutor(),
                dto.getDescripcion(),
                estadoNormalizado,
                dto.getUbicacion(),
                dto.getUrlsImagenes()
        );
    }

    private LibroHomeDTO toDto(LibroHomeProjection p) {
        return new LibroHomeDTO(
                p.getTitulo(),
                p.getAutor(),
                null,
                p.getUrl_portada(),
                null
        );
    }

    private LibroDTO toBusquedaDto(LibroHomeProjection p) {
        return new LibroDTO(
                p.getId_libro(),
                p.getTitulo(),
                p.getAutor(),
                p.getUrl_portada()
        );
    }

    private String normalizarEstado(String estado) {
        if (estado == null) {
            return null;
        }
        String value = estado.trim().toLowerCase();
        if (value.isBlank()) {
            return null;
        }
        if ("como nuevo".equals(value)) {
            return "muy bueno";
        }
        return value;
    }
}
