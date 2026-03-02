package com.bardales.intercambiolibrosapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bardales.intercambiolibrosapi.entity.Libro;

public interface LibroRepository extends JpaRepository<Libro, Integer> {

    @Query(value = "SELECT l.id_libro AS id_libro, l.id_usuario AS id_usuario, "
            + "l.titulo AS titulo, l.autor AS autor, img.url_imagen AS url_portada, "
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
            "ORDER BY l.fecha_registro DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<LibroHomeProjection> listarRecientes(@Param("limit") int limit, @Param("offset") int offset);

    @Query(value = "SELECT l.id_libro AS id_libro, l.id_usuario AS id_usuario, "
            + "l.titulo AS titulo, l.autor AS autor, img.url_imagen AS url_portada, "
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
            "LEFT JOIN LATERAL ( "
            + "SELECT ux.direccion, ux.ciudad FROM ubicacion ux "
            + "WHERE ux.id_usuario = :idUsuario "
            + "ORDER BY ux.fecha_actualizacion DESC NULLS LAST, ux.id_ubicacion DESC "
            + "LIMIT 1 "
            + ") uu ON TRUE "
            +
            "WHERE (:titulo IS NULL OR l.titulo ILIKE CONCAT('%', :titulo, '%')) "
            +
            "AND (:autor IS NULL OR l.autor ILIKE CONCAT('%', :autor, '%')) "
            +
            "AND (:idCategoria IS NULL OR l.id_categoria = :idCategoria OR EXISTS ("
            + "SELECT 1 FROM libro_categoria lc WHERE lc.id_libro = l.id_libro AND lc.id_categoria = :idCategoria"
            + ")) "
            +
            "AND (:estado IS NULL OR l.estado = :estado) "
            +
            "AND (:idUsuario IS NULL OR l.id_usuario <> :idUsuario) "
            +
            "AND ("
            + ":alcance <> 'local' "
            + "OR :idUsuario IS NULL "
            + "OR uu.ciudad IS NULL "
            + "OR ("
            + "uo.ciudad IS NOT NULL AND LOWER(TRIM(uu.ciudad)) = LOWER(TRIM(uo.ciudad)) "
            + "AND ("
            + "COALESCE(NULLIF(TRIM(uu.direccion), ''), '') = '' "
            + "OR COALESCE(NULLIF(TRIM(uo.direccion), ''), '') = '' "
            + "OR LOWER(TRIM(uu.direccion)) = LOWER(TRIM(uo.direccion))"
            + ")"
            + ")"
            + ") "
            +
            "ORDER BY l.fecha_registro DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<LibroHomeProjection> buscarLibros(
            @Param("titulo") String titulo,
            @Param("autor") String autor,
            @Param("idCategoria") Integer idCategoria,
            @Param("estado") String estado,
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
}
