package com.bardales.intercambiolibrosapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bardales.intercambiolibrosapi.entity.Libro;

public interface LibroRepository extends JpaRepository<Libro, Integer> {

    @Query(value = "SELECT l.id_libro AS id_libro, l.id_usuario AS id_usuario, "
            + "l.titulo AS titulo, l.autor AS autor, img.url_imagen AS url_portada, l.situacion AS situacion, "
            + "uo.direccion AS distrito, uo.ciudad AS departamento "
            +
            "FROM libro l "
            +
            "LEFT JOIN (SELECT i.id_libro, MIN(i.id_imagen) AS id_imagen FROM imagen_libro i GROUP BY i.id_libro) img_min ON img_min.id_libro = l.id_libro "
            +
            "LEFT JOIN imagen_libro img ON img.id_imagen = img_min.id_imagen "
            +
            "LEFT JOIN LATERAL ( "
            + "SELECT ux.direccion, ux.ciudad FROM ubicacion ux "
            + "WHERE ux.id_usuario = l.id_usuario "
            + "ORDER BY ux.fecha_actualizacion DESC NULLS LAST, ux.id_ubicacion DESC "
            + "LIMIT 1 "
            + ") uo ON TRUE "
            +
            "WHERE COALESCE(l.estado, 'activo') = 'activo' "
            +
            "ORDER BY l.fecha_registro DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<LibroHomeProjection> listarRecientes(@Param("limit") int limit, @Param("offset") int offset);

    @Query(value = "SELECT l.id_libro AS id_libro, l.id_usuario AS id_usuario, "
            + "l.titulo AS titulo, l.autor AS autor, img.url_imagen AS url_portada, l.situacion AS situacion, "
            + "uo.direccion AS distrito, uo.ciudad AS departamento "
            +
            "FROM libro l "
            +
            "LEFT JOIN (SELECT i.id_libro, MIN(i.id_imagen) AS id_imagen FROM imagen_libro i GROUP BY i.id_libro) img_min ON img_min.id_libro = l.id_libro "
            +
            "LEFT JOIN imagen_libro img ON img.id_imagen = img_min.id_imagen "
            +
            "LEFT JOIN LATERAL ( "
            + "SELECT ux.direccion, ux.ciudad, ux.latitud, ux.longitud FROM ubicacion ux "
            + "WHERE ux.id_usuario = l.id_usuario "
            + "ORDER BY ux.fecha_actualizacion DESC NULLS LAST, ux.id_ubicacion DESC "
            + "LIMIT 1 "
            + ") uo ON TRUE "
            +
            "LEFT JOIN LATERAL ( "
            + "SELECT ux.direccion, ux.ciudad, ux.latitud, ux.longitud FROM ubicacion ux "
            + "WHERE ux.id_usuario = :idUsuario "
            + "ORDER BY ux.fecha_actualizacion DESC NULLS LAST, ux.id_ubicacion DESC "
            + "LIMIT 1 "
            + ") uu ON TRUE "
            +
            "WHERE ("
            + "(:titulo IS NULL AND :autor IS NULL) "
            + "OR (:titulo IS NOT NULL AND l.titulo ILIKE CONCAT('%', :titulo, '%')) "
            + "OR (:autor IS NOT NULL AND l.autor ILIKE CONCAT('%', :autor, '%'))"
            + ") "
            +
            "AND (:idCategoria IS NULL OR l.id_categoria = :idCategoria OR EXISTS ("
            + "SELECT 1 FROM libro_categoria lc WHERE lc.id_libro = l.id_libro AND lc.id_categoria = :idCategoria"
            + ")) "
            +
            "AND (:condicion IS NULL OR l.condicion = :condicion) "
            +
            "AND COALESCE(l.estado, 'activo') = 'activo' "
            +
            "AND (:idUsuario IS NULL OR l.id_usuario <> :idUsuario) "
            +
            "AND ("
            + ":idUsuario IS NULL "
            + "OR :alcance IN ('internacional', 'amplia') "
            + "OR uu.ciudad IS NULL "
            + "OR ("
            + ":alcance = 'nacional' "
            + "AND uo.ciudad IS NOT NULL "
            + "AND LOWER(TRIM(uu.ciudad)) = LOWER(TRIM(uo.ciudad))"
            + ") "
            + "OR ("
            + ":alcance = 'local' "
            + "AND uo.ciudad IS NOT NULL "
            + "AND LOWER(TRIM(uu.ciudad)) = LOWER(TRIM(uo.ciudad)) "
            + "AND ("
            + "COALESCE(NULLIF(TRIM(uu.direccion), ''), '') = '' "
            + "OR COALESCE(NULLIF(TRIM(uo.direccion), ''), '') = '' "
            + "OR LOWER(TRIM(uu.direccion)) = LOWER(TRIM(uo.direccion))"
            + ")"
            + ") "
            + ") "
            +
            "ORDER BY "
            + "CASE "
            + "WHEN :idUsuario IS NULL OR uu.ciudad IS NULL OR uo.ciudad IS NULL THEN 2 "
            + "WHEN LOWER(TRIM(uu.ciudad)) = LOWER(TRIM(uo.ciudad)) "
            + "AND COALESCE(NULLIF(TRIM(uu.direccion), ''), '') <> '' "
            + "AND COALESCE(NULLIF(TRIM(uo.direccion), ''), '') <> '' "
            + "AND LOWER(TRIM(uu.direccion)) = LOWER(TRIM(uo.direccion)) THEN 0 "
            + "WHEN LOWER(TRIM(uu.ciudad)) = LOWER(TRIM(uo.ciudad)) THEN 1 "
            + "ELSE 2 END ASC, "
            + "CASE "
            + "WHEN :idUsuario IS NULL THEN NULL "
            + "WHEN uu.latitud IS NULL OR uu.longitud IS NULL OR uo.latitud IS NULL OR uo.longitud IS NULL THEN NULL "
            + "ELSE POWER(uu.latitud - uo.latitud, 2) + POWER(uu.longitud - uo.longitud, 2) END ASC NULLS LAST, "
            + "l.fecha_registro DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<LibroHomeProjection> buscarLibros(
            @Param("titulo") String titulo,
            @Param("autor") String autor,
            @Param("idCategoria") Integer idCategoria,
            @Param("condicion") String condicion,
            @Param("idUsuario") Integer idUsuario,
            @Param("alcance") String alcance,
            @Param("limit") int limit,
            @Param("offset") int offset);

    @Query(value = "SELECT l.id_libro AS id_libro, l.id_usuario AS id_usuario, "
            + "l.titulo AS titulo, l.autor AS autor, img.url_imagen AS url_portada, l.situacion AS situacion, "
            + "uo.direccion AS distrito, uo.ciudad AS departamento "
            +
            "FROM libro l "
            +
            "LEFT JOIN (SELECT i.id_libro, MIN(i.id_imagen) AS id_imagen FROM imagen_libro i GROUP BY i.id_libro) img_min ON img_min.id_libro = l.id_libro "
            +
            "LEFT JOIN imagen_libro img ON img.id_imagen = img_min.id_imagen "
            +
            "LEFT JOIN LATERAL ( "
            + "SELECT ux.direccion, ux.ciudad, ux.latitud, ux.longitud FROM ubicacion ux "
            + "WHERE ux.id_usuario = l.id_usuario "
            + "ORDER BY ux.fecha_actualizacion DESC NULLS LAST, ux.id_ubicacion DESC "
            + "LIMIT 1 "
            + ") uo ON TRUE "
            +
            "LEFT JOIN LATERAL ( "
            + "SELECT ux.direccion, ux.ciudad, ux.latitud, ux.longitud FROM ubicacion ux "
            + "WHERE ux.id_usuario = :idUsuario "
            + "ORDER BY ux.fecha_actualizacion DESC NULLS LAST, ux.id_ubicacion DESC "
            + "LIMIT 1 "
            + ") uu ON TRUE "
            +
            "WHERE ("
            + "(:titulo IS NULL AND :autor IS NULL) "
            + "OR (:titulo IS NOT NULL AND l.titulo ILIKE CONCAT('%', :titulo, '%')) "
            + "OR (:autor IS NOT NULL AND l.autor ILIKE CONCAT('%', :autor, '%'))"
            + ") "
            +
            "AND (:idCategoria IS NULL OR l.id_categoria = :idCategoria) "
            +
            "AND (:condicion IS NULL OR l.condicion = :condicion) "
            +
            "AND COALESCE(l.estado, 'activo') = 'activo' "
            +
            "AND (:idUsuario IS NULL OR l.id_usuario <> :idUsuario) "
            +
            "AND ("
            + ":idUsuario IS NULL "
            + "OR :alcance IN ('internacional', 'amplia') "
            + "OR uu.ciudad IS NULL "
            + "OR ("
            + ":alcance = 'nacional' "
            + "AND uo.ciudad IS NOT NULL "
            + "AND LOWER(TRIM(uu.ciudad)) = LOWER(TRIM(uo.ciudad))"
            + ") "
            + "OR ("
            + ":alcance = 'local' "
            + "AND uo.ciudad IS NOT NULL "
            + "AND LOWER(TRIM(uu.ciudad)) = LOWER(TRIM(uo.ciudad)) "
            + "AND ("
            + "COALESCE(NULLIF(TRIM(uu.direccion), ''), '') = '' "
            + "OR COALESCE(NULLIF(TRIM(uo.direccion), ''), '') = '' "
            + "OR LOWER(TRIM(uu.direccion)) = LOWER(TRIM(uo.direccion))"
            + ")"
            + ") "
            + ") "
            +
            "ORDER BY "
            + "CASE "
            + "WHEN :idUsuario IS NULL OR uu.ciudad IS NULL OR uo.ciudad IS NULL THEN 2 "
            + "WHEN LOWER(TRIM(uu.ciudad)) = LOWER(TRIM(uo.ciudad)) "
            + "AND COALESCE(NULLIF(TRIM(uu.direccion), ''), '') <> '' "
            + "AND COALESCE(NULLIF(TRIM(uo.direccion), ''), '') <> '' "
            + "AND LOWER(TRIM(uu.direccion)) = LOWER(TRIM(uo.direccion)) THEN 0 "
            + "WHEN LOWER(TRIM(uu.ciudad)) = LOWER(TRIM(uo.ciudad)) THEN 1 "
            + "ELSE 2 END ASC, "
            + "CASE "
            + "WHEN :idUsuario IS NULL THEN NULL "
            + "WHEN uu.latitud IS NULL OR uu.longitud IS NULL OR uo.latitud IS NULL OR uo.longitud IS NULL THEN NULL "
            + "ELSE POWER(uu.latitud - uo.latitud, 2) + POWER(uu.longitud - uo.longitud, 2) END ASC NULLS LAST, "
            + "l.fecha_registro DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<LibroHomeProjection> buscarLibrosLegacy(
            @Param("titulo") String titulo,
            @Param("autor") String autor,
            @Param("idCategoria") Integer idCategoria,
            @Param("condicion") String condicion,
            @Param("idUsuario") Integer idUsuario,
            @Param("alcance") String alcance,
            @Param("limit") int limit,
            @Param("offset") int offset);

    @Modifying
    @Query(value = "INSERT INTO imagen_libro (id_libro, url_imagen) VALUES (:idLibro, :urlImagen)", nativeQuery = true)
    void vincularImagen(@Param("idLibro") Integer idLibro, @Param("urlImagen") String urlImagen);

    @Modifying
    @Query(value = "INSERT INTO libro_categoria (id_libro, id_categoria) VALUES (:idLibro, :idCategoria) ON CONFLICT DO NOTHING", nativeQuery = true)
    void vincularCategoria(@Param("idLibro") Integer idLibro, @Param("idCategoria") Integer idCategoria);

    Optional<Libro> findByIdLibroAndIdUsuario(Integer idLibro, Integer idUsuario);

    @Modifying
    @Query(value = "DELETE FROM imagen_libro WHERE id_libro = :idLibro", nativeQuery = true)
    void eliminarImagenesPorLibro(@Param("idLibro") Integer idLibro);

    @Modifying
    @Query(value = "DELETE FROM libro_categoria WHERE id_libro = :idLibro", nativeQuery = true)
    void eliminarCategoriasPorLibro(@Param("idLibro") Integer idLibro);
}
