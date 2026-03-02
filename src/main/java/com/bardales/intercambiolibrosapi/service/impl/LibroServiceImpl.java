package com.bardales.intercambiolibrosapi.service.impl;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bardales.intercambiolibrosapi.dto.LibroActualizarDTO;
import com.bardales.intercambiolibrosapi.dto.LibroCreadoDTO;
import com.bardales.intercambiolibrosapi.dto.LibroDTO;
import com.bardales.intercambiolibrosapi.dto.LibroHomeDTO;
import com.bardales.intercambiolibrosapi.dto.LibroRegistroDTO;
import com.bardales.intercambiolibrosapi.entity.Libro;
import com.bardales.intercambiolibrosapi.exception.ResourceNotFoundException;
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

            jdbcTemplate.execute("ALTER TABLE IF EXISTS libro ADD COLUMN IF NOT EXISTS situacion VARCHAR(30)");
            jdbcTemplate.execute("ALTER TABLE IF EXISTS libro ADD COLUMN IF NOT EXISTS estado_logico VARCHAR(20)");
            jdbcTemplate.update(
                    "UPDATE libro SET situacion = CASE WHEN COALESCE(disponible, TRUE) THEN 'disponible' ELSE 'ocupado' END "
                            + "WHERE situacion IS NULL OR TRIM(situacion) = ''");
            jdbcTemplate.update("UPDATE libro SET estado_logico = 'activo' WHERE estado_logico IS NULL OR TRIM(estado_logico) = ''");
            jdbcTemplate.execute("ALTER TABLE IF EXISTS libro ALTER COLUMN situacion SET DEFAULT 'disponible'");
            jdbcTemplate.execute("ALTER TABLE IF EXISTS libro ALTER COLUMN estado_logico SET DEFAULT 'activo'");
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
        String alcanceNormalizado = normalizarAlcance(alcance);

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
        String situacionNormalizada = normalizarSituacion(dto.getSituacion());
        String estadoLogico = "activo";
        boolean disponible = esDisponible(situacionNormalizada);

        List<Integer> categorias = normalizarCategorias(dto.getIdCategorias(), dto.getIdCategoria());
        if (categorias.isEmpty()) {
            throw new RuntimeException("Debes seleccionar al menos una categoria");
        }
        Integer categoriaPrincipal = categorias.get(0);

        List<String> imagenes = normalizarUrls(dto.getUrlsImagenes());

        Integer idLibro = jdbcTemplate.queryForObject(
                "INSERT INTO libro (id_usuario, id_categoria, titulo, autor, descripcion, estado, situacion, estado_logico, ubicacion, disponible) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_libro",
                Integer.class,
                idUsuario,
                categoriaPrincipal,
                dto.getTitulo(),
                dto.getAutor(),
                dto.getDescripcion(),
                estadoNormalizado,
                situacionNormalizada,
                estadoLogico,
                dto.getUbicacion(),
                disponible);

        if (idLibro == null || idLibro <= 0) {
            throw new RuntimeException("No se pudo registrar el libro");
        }

        for (String url : imagenes) {
            libroRepository.vincularImagen(idLibro, url);
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
                situacionNormalizada,
                estadoLogico,
                dto.getUbicacion(),
                imagenes
        );
    }

    @Override
    @Transactional
    public LibroCreadoDTO actualizarLibro(int idUsuario, int idLibro, LibroActualizarDTO dto) {
        Libro libro = libroRepository.findByIdLibroAndIdUsuario(idLibro, idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado"));

        if (!"activo".equalsIgnoreCase(normalizarEstadoLogico(libro.getEstadoLogico()))) {
            throw new ResourceNotFoundException("El libro esta inactivo y no se puede editar");
        }

        List<Integer> categoriasNuevas = normalizarCategorias(dto.getIdCategorias(), dto.getIdCategoria());
        Integer categoriaPrincipal = !categoriasNuevas.isEmpty()
                ? categoriasNuevas.get(0)
                : libro.getIdCategoria();

        String titulo = conservarSiVacio(dto.getTitulo(), libro.getTitulo());
        String autor = conservarSiVacio(dto.getAutor(), libro.getAutor());
        String descripcion = dto.getDescripcion() == null ? libro.getDescripcion() : valorNullable(dto.getDescripcion());
        String ubicacion = dto.getUbicacion() == null ? libro.getUbicacion() : valorNullable(dto.getUbicacion());

        String estado = dto.getEstado() == null ? libro.getEstado() : normalizarEstado(dto.getEstado());
        String situacion = dto.getSituacion() == null
                ? normalizarSituacion(libro.getSituacion())
                : normalizarSituacion(dto.getSituacion());
        boolean disponible = esDisponible(situacion);

        jdbcTemplate.update(
                "UPDATE libro SET id_categoria = ?, titulo = ?, autor = ?, descripcion = ?, estado = ?, situacion = ?, ubicacion = ?, disponible = ? WHERE id_libro = ?",
                categoriaPrincipal,
                titulo,
                autor,
                descripcion,
                estado,
                situacion,
                ubicacion,
                disponible,
                idLibro);

        if (!categoriasNuevas.isEmpty()) {
            libroRepository.eliminarCategoriasPorLibro(idLibro);
            for (Integer idCategoria : categoriasNuevas) {
                try {
                    libroRepository.vincularCategoria(idLibro, idCategoria);
                } catch (DataAccessException ex) {
                    if (!isMissingLibroCategoria(ex)) {
                        throw ex;
                    }
                    LOGGER.warn("No se pudo actualizar categorias N:N por falta de tabla libro_categoria");
                }
            }
        }

        if (dto.getUrlsImagenes() != null) {
            List<String> urls = normalizarUrls(dto.getUrlsImagenes());
            libroRepository.eliminarImagenesPorLibro(idLibro);
            for (String url : urls) {
                libroRepository.vincularImagen(idLibro, url);
            }
        }

        List<Integer> categoriasDto = categoriasNuevas.isEmpty() ? obtenerCategoriasLibro(idLibro, categoriaPrincipal) : categoriasNuevas;
        List<String> imagenesDto = obtenerImagenesLibro(idLibro);

        return new LibroCreadoDTO(
                idLibro,
                idUsuario,
                categoriaPrincipal,
                categoriasDto,
                titulo,
                autor,
                descripcion,
                estado,
                situacion,
                "activo",
                ubicacion,
                imagenesDto);
    }

    @Override
    @Transactional
    public Map<String, Object> eliminarLibroLogico(int idUsuario, int idLibro) {
        Libro libro = libroRepository.findByIdLibroAndIdUsuario(idLibro, idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado"));

        if ("inactivo".equalsIgnoreCase(normalizarEstadoLogico(libro.getEstadoLogico()))) {
            return Map.of(
                    "mensaje", "El libro ya estaba inactivo",
                    "idLibro", idLibro,
                    "estadoLogico", "inactivo");
        }

        jdbcTemplate.update(
                "UPDATE libro SET estado_logico = 'inactivo', situacion = 'ocupado', disponible = FALSE WHERE id_libro = ? AND id_usuario = ?",
                idLibro,
                idUsuario);

        return Map.of(
                "mensaje", "Libro eliminado logicamente",
                "idLibro", idLibro,
                "estadoLogico", "inactivo",
                "situacion", "ocupado");
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
                p.getSituacion(),
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

    private String normalizarAlcance(String alcance) {
        if (alcance == null || alcance.isBlank()) {
            return "internacional";
        }
        String value = alcance.trim().toLowerCase();
        if ("amplia".equals(value)) {
            return "internacional";
        }
        if (!"local".equals(value) && !"nacional".equals(value) && !"internacional".equals(value)) {
            return "internacional";
        }
        return value;
    }

    private String normalizarSituacion(String situacion) {
        if (situacion == null || situacion.isBlank()) {
            return "disponible";
        }
        String value = situacion.trim().toLowerCase();
        if ("libre".equals(value)) {
            return "disponible";
        }
        if ("no disponible".equals(value) || "reservado".equals(value)) {
            return "ocupado";
        }
        if (!"disponible".equals(value) && !"ocupado".equals(value)) {
            return "disponible";
        }
        return value;
    }

    private String normalizarEstadoLogico(String estadoLogico) {
        if (estadoLogico == null || estadoLogico.isBlank()) {
            return "activo";
        }
        return estadoLogico.trim().toLowerCase();
    }

    private boolean esDisponible(String situacion) {
        return "disponible".equalsIgnoreCase(normalizarSituacion(situacion));
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

    private List<String> normalizarUrls(List<String> urls) {
        if (urls == null) {
            return List.of();
        }
        Set<String> depuradas = new LinkedHashSet<>();
        for (String url : urls) {
            if (url == null) {
                continue;
            }
            String valor = url.trim();
            if (!valor.isEmpty()) {
                depuradas.add(valor);
            }
        }
        return List.copyOf(depuradas);
    }

    private List<String> obtenerImagenesLibro(int idLibro) {
        return jdbcTemplate.query(
                "SELECT url_imagen FROM imagen_libro WHERE id_libro = ? ORDER BY id_imagen",
                (rs, rowNum) -> rs.getString("url_imagen"),
                idLibro);
    }

    private List<Integer> obtenerCategoriasLibro(int idLibro, Integer categoriaPrincipal) {
        try {
            List<Integer> categorias = jdbcTemplate.queryForList(
                    "SELECT id_categoria FROM libro_categoria WHERE id_libro = ? ORDER BY id_categoria",
                    Integer.class,
                    idLibro);
            if (!categorias.isEmpty()) {
                return categorias;
            }
        } catch (DataAccessException ex) {
            if (!isMissingLibroCategoria(ex)) {
                throw ex;
            }
        }
        if (categoriaPrincipal != null) {
            return List.of(categoriaPrincipal);
        }
        return List.of();
    }

    private String conservarSiVacio(String nuevoValor, String valorActual) {
        if (nuevoValor == null) {
            return valorActual;
        }
        String valor = nuevoValor.trim();
        if (valor.isEmpty()) {
            return valorActual;
        }
        return valor;
    }

    private String valorNullable(String value) {
        String normalized = value == null ? null : value.trim();
        if (normalized == null || normalized.isEmpty()) {
            return null;
        }
        return normalized;
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
