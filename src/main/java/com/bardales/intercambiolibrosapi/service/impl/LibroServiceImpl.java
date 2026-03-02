package com.bardales.intercambiolibrosapi.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(LibroServiceImpl.class);

    private final LibroRepository libroRepository;
    private final JdbcTemplate jdbcTemplate;

    public LibroServiceImpl(LibroRepository libroRepository, JdbcTemplate jdbcTemplate) {
        this.libroRepository = libroRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    void inicializarEsquemaLibroCategoria() {
        try {
            jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS libro_categoria ("
                            + "id_libro INT NOT NULL REFERENCES libro(id_libro) ON DELETE CASCADE, "
                            + "id_categoria INT NOT NULL REFERENCES categoria(id_categoria) ON DELETE CASCADE, "
                            + "PRIMARY KEY (id_libro, id_categoria)"
                            + ")");
            jdbcTemplate.update(
                    "INSERT INTO libro_categoria (id_libro, id_categoria) "
                            + "SELECT l.id_libro, l.id_categoria FROM libro l "
                            + "WHERE l.id_categoria IS NOT NULL "
                            + "ON CONFLICT DO NOTHING");
            jdbcTemplate.execute("ALTER TABLE IF EXISTS imagen_libro ALTER COLUMN url_imagen TYPE TEXT");
        } catch (Exception ex) {
            LOGGER.warn("No se pudieron aplicar ajustes de esquema de libros automaticamente: {}", ex.getMessage());
        }
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
    public List<LibroDTO> buscarLibros(
            String query,
            Integer idCategoria,
            String estado,
            Integer idUsuario,
            String alcance,
            int pagina,
            int cantidad) {
        int safePagina = Math.max(pagina, 1);
        int safeCantidad = Math.max(cantidad, 1);
        int offset = (safePagina - 1) * safeCantidad;
        String filtro = (query == null || query.isBlank()) ? null : query.trim();
        String estadoFiltro = (estado == null || estado.isBlank()) ? null : estado.trim();
        String alcanceNormalizado = (alcance == null || alcance.isBlank()) ? "amplia" : alcance.trim().toLowerCase();

        try {
            return libroRepository.buscarLibros(
                            filtro,
                            filtro,
                            idCategoria,
                            estadoFiltro,
                            idUsuario,
                            alcanceNormalizado,
                            safeCantidad,
                            offset)
                    .stream()
                    .map(this::toBusquedaDto)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            if (isMissingLibroCategoria(ex)) {
                LOGGER.warn("Fallback de busqueda sin libro_categoria por esquema desactualizado");
                return libroRepository.buscarLibrosLegacy(
                                filtro,
                                filtro,
                                idCategoria,
                                estadoFiltro,
                                idUsuario,
                                alcanceNormalizado,
                                safeCantidad,
                                offset)
                        .stream()
                        .map(this::toBusquedaDto)
                        .collect(Collectors.toList());
            }
            throw ex;
        }
    }

    @Override
    @Transactional
    public LibroCreadoDTO registrarLibro(int idUsuario, LibroRegistroDTO dto) {
        String estadoNormalizado = normalizarEstado(dto.getEstado());
        List<Integer> categorias = normalizarCategorias(dto.getIdCategorias(), dto.getIdCategoria());
        if (categorias.isEmpty()) {
            throw new RuntimeException("Debes seleccionar al menos una categoria");
        }
        Integer categoriaPrincipal = categorias.get(0);

        Integer idLibro = jdbcTemplate.queryForObject(
                "INSERT INTO libro (id_usuario, id_categoria, titulo, autor, descripcion, estado, ubicacion, disponible) "
                        +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, TRUE) RETURNING id_libro",
                Integer.class,
                idUsuario,
                categoriaPrincipal,
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

        for (Integer idCategoria : categorias) {
            try {
                libroRepository.vincularCategoria(idLibro, idCategoria);
            } catch (DataAccessException ex) {
                if (!isMissingLibroCategoria(ex)) {
                    throw ex;
                }
                LOGGER.warn("No se pudo vincular categoria N:N por falta de tabla libro_categoria");
            }
        }

        return new LibroCreadoDTO(
                idLibro,
                idUsuario,
                categoriaPrincipal,
                categorias,
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
                p.getUrl_portada(),
                p.getId_usuario(),
                p.getDistrito(),
                p.getDepartamento()
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

    private List<Integer> normalizarCategorias(List<Integer> idCategorias, Integer idCategoria) {
        List<Integer> categorias = idCategorias == null ? List.of() : idCategorias;
        List<Integer> depuradas = categorias.stream()
                .filter(Objects::nonNull)
                .filter(id -> id > 0)
                .distinct()
                .collect(Collectors.toList());
        if (!depuradas.isEmpty()) {
            return depuradas;
        }
        if (idCategoria != null && idCategoria > 0) {
            return List.of(idCategoria);
        }
        return List.of();
    }

    private boolean isMissingLibroCategoria(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && message.toLowerCase().contains("libro_categoria")
                    && message.toLowerCase().contains("does not exist")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
