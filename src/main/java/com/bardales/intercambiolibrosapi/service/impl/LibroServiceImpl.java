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
import com.bardales.intercambiolibrosapi.exception.UnauthorizedException;
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
            jdbcTemplate.execute("ALTER TABLE IF EXISTS libro ADD COLUMN IF NOT EXISTS estado VARCHAR(20)");
            jdbcTemplate.execute("ALTER TABLE IF EXISTS libro ADD COLUMN IF NOT EXISTS condicion VARCHAR(30)");

            jdbcTemplate.update(
                    "UPDATE libro SET condicion = CASE "
                            + "WHEN LOWER(TRIM(COALESCE(estado, ''))) = 'regular' THEN 'aceptable' "
                            + "WHEN LOWER(TRIM(COALESCE(estado, ''))) IN ('nuevo', 'como nuevo', 'muy bueno', 'bueno', 'aceptable') "
                            + "THEN LOWER(TRIM(estado)) "
                            + "ELSE 'bueno' END "
                            + "WHERE condicion IS NULL OR TRIM(condicion) = ''");

            jdbcTemplate.execute(
                    "DO $$ BEGIN "
                            + "IF EXISTS ("
                            + "SELECT 1 FROM information_schema.columns "
                            + "WHERE table_schema = 'public' AND table_name = 'libro' AND column_name = 'estado_logico'"
                            + ") THEN "
                            + "UPDATE libro SET estado = CASE "
                            + "WHEN LOWER(TRIM(COALESCE(estado, ''))) IN ('activo', 'inactivo') THEN LOWER(TRIM(estado)) "
                            + "WHEN LOWER(TRIM(COALESCE(estado_logico, ''))) IN ('activo', 'inactivo') THEN LOWER(TRIM(estado_logico)) "
                            + "ELSE 'activo' END "
                            + "WHERE estado IS NULL OR TRIM(estado) = '' OR LOWER(TRIM(estado)) NOT IN ('activo', 'inactivo'); "
                            + "ELSE "
                            + "UPDATE libro SET estado = CASE "
                            + "WHEN LOWER(TRIM(COALESCE(estado, ''))) IN ('activo', 'inactivo') THEN LOWER(TRIM(estado)) "
                            + "ELSE 'activo' END "
                            + "WHERE estado IS NULL OR TRIM(estado) = '' OR LOWER(TRIM(estado)) NOT IN ('activo', 'inactivo'); "
                            + "END IF; "
                            + "END $$;");

            jdbcTemplate.update(
                    "UPDATE libro SET situacion = CASE WHEN COALESCE(disponible, TRUE) THEN 'disponible' ELSE 'ocupado' END "
                            + "WHERE situacion IS NULL OR TRIM(situacion) = ''");
            jdbcTemplate.execute("ALTER TABLE IF EXISTS libro ALTER COLUMN situacion SET DEFAULT 'disponible'");
            jdbcTemplate.execute("ALTER TABLE IF EXISTS libro ALTER COLUMN estado SET DEFAULT 'activo'");
            jdbcTemplate.execute("ALTER TABLE IF EXISTS libro ALTER COLUMN condicion SET DEFAULT 'bueno'");
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
            String condicion,
            Integer idUsuario,
            String alcance,
            int pagina,
            int cantidad) {
        int safePagina = Math.max(pagina, 1);
        int safeCantidad = Math.max(cantidad, 1);
        int offset = (safePagina - 1) * safeCantidad;
        String filtro = (query == null || query.isBlank()) ? null : query.trim();
        String condicionFiltro = (condicion == null || condicion.isBlank()) ? null : condicion.trim();
        String alcanceNormalizado = normalizarAlcance(alcance);
        if (filtro != null) {
            // Si se busca por texto (titulo/autor), mostrar todos y ordenar por cercania.
            alcanceNormalizado = "internacional";
        }

        try {
            return libroRepository.buscarLibros(
                            filtro,
                            filtro,
                            idCategoria,
                            condicionFiltro,
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
                                condicionFiltro,
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
        String estadoNormalizado = "activo";
        String condicionNormalizada = normalizarCondicion(dto.getCondicion());
        String situacionNormalizada = normalizarSituacion(dto.getSituacion());
        boolean disponible = esDisponible(situacionNormalizada);

        int puntosConsumidos = jdbcTemplate.update(
                "UPDATE usuario SET puntos = COALESCE(puntos, 0) - 1 "
                        + "WHERE id_usuario = ? AND COALESCE(puntos, 0) > 0",
                idUsuario);
        if (puntosConsumidos == 0) {
            throw new UnauthorizedException("No tienes puntos suficientes. Mira un anuncio para ganar 1 punto.");
        }

        List<Integer> categorias = normalizarCategorias(dto.getIdCategorias(), dto.getIdCategoria());
        if (categorias.isEmpty()) {
            throw new RuntimeException("Debes seleccionar al menos una categoria");
        }
        Integer categoriaPrincipal = categorias.get(0);

        List<String> imagenes = normalizarUrls(dto.getUrlsImagenes());

        Integer idLibro = jdbcTemplate.queryForObject(
                "INSERT INTO libro (id_usuario, id_categoria, titulo, autor, descripcion, estado, condicion, situacion, ubicacion, disponible) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_libro",
                Integer.class,
                idUsuario,
                categoriaPrincipal,
                dto.getTitulo(),
                dto.getAutor(),
                dto.getDescripcion(),
                estadoNormalizado,
                condicionNormalizada,
                situacionNormalizada,
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
                condicionNormalizada,
                situacionNormalizada,
                dto.getUbicacion(),
                imagenes
        );
    }

    @Override
    @Transactional
    public LibroCreadoDTO actualizarLibro(int idUsuario, int idLibro, LibroActualizarDTO dto) {
        Libro libro = libroRepository.findByIdLibroAndIdUsuario(idLibro, idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado"));

        if (!"activo".equalsIgnoreCase(normalizarEstadoLibro(libro.getEstado()))) {
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

        String estado = normalizarEstadoLibro(libro.getEstado());
        String condicion = dto.getCondicion() == null
                ? normalizarCondicion(libro.getCondicion())
                : normalizarCondicion(dto.getCondicion());
        String situacion = dto.getSituacion() == null
                ? normalizarSituacion(libro.getSituacion())
                : normalizarSituacion(dto.getSituacion());
        boolean disponible = esDisponible(situacion);

        jdbcTemplate.update(
                "UPDATE libro SET id_categoria = ?, titulo = ?, autor = ?, descripcion = ?, estado = ?, condicion = ?, situacion = ?, ubicacion = ?, disponible = ? WHERE id_libro = ?",
                categoriaPrincipal,
                titulo,
                autor,
                descripcion,
                estado,
                condicion,
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
                condicion,
                situacion,
                ubicacion,
                imagenesDto);
    }

    @Override
    @Transactional
    public Map<String, Object> eliminarLibroLogico(int idUsuario, int idLibro) {
        Libro libro = libroRepository.findByIdLibroAndIdUsuario(idLibro, idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado"));

        if ("inactivo".equalsIgnoreCase(normalizarEstadoLibro(libro.getEstado()))) {
            return Map.of(
                    "mensaje", "El libro ya estaba inactivo",
                    "idLibro", idLibro,
                    "estado", "inactivo");
        }

        jdbcTemplate.update(
                "UPDATE libro SET estado = 'inactivo', situacion = 'ocupado', disponible = FALSE WHERE id_libro = ? AND id_usuario = ?",
                idLibro,
                idUsuario);

        return Map.of(
                "mensaje", "Libro eliminado logicamente",
                "idLibro", idLibro,
                "estado", "inactivo",
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

    private String normalizarCondicion(String condicion) {
        if (condicion == null || condicion.isBlank()) {
            return "bueno";
        }
        String value = condicion.trim().toLowerCase();
        if ("regular".equals(value)) {
            return "aceptable";
        }
        if (!"nuevo".equals(value)
                && !"como nuevo".equals(value)
                && !"muy bueno".equals(value)
                && !"bueno".equals(value)
                && !"aceptable".equals(value)) {
            return "bueno";
        }
        return value;
    }

    private String normalizarEstadoLibro(String estado) {
        if (estado == null || estado.isBlank()) {
            return "activo";
        }
        String value = estado.trim().toLowerCase();
        if (!"activo".equals(value) && !"inactivo".equals(value)) {
            return "activo";
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
