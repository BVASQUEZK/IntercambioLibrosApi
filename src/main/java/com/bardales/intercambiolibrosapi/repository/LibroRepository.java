package com.bardales.intercambiolibrosapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bardales.intercambiolibrosapi.entity.Libro;

public interface LibroRepository extends JpaRepository<Libro, Integer> {

    @Query(value = "SELECT l.id_libro AS id_libro, l.titulo AS titulo, l.autor AS autor, img.url_imagen AS url_portada "
            +
            "FROM libro l "
            +
            "LEFT JOIN (SELECT i.id_libro, MIN(i.id_imagen) AS id_imagen FROM imagen_libro i GROUP BY i.id_libro) img_min ON img_min.id_libro = l.id_libro "
            +
            "LEFT JOIN imagen_libro img ON img.id_imagen = img_min.id_imagen "
            +
            "ORDER BY l.fecha_registro DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<LibroHomeProjection> listarRecientes(@Param("limit") int limit, @Param("offset") int offset);

    @Query(value = "SELECT l.id_libro AS id_libro, l.titulo AS titulo, l.autor AS autor, img.url_imagen AS url_portada "
            +
            "FROM libro l "
            +
            "LEFT JOIN (SELECT i.id_libro, MIN(i.id_imagen) AS id_imagen FROM imagen_libro i GROUP BY i.id_libro) img_min ON img_min.id_libro = l.id_libro "
            +
            "LEFT JOIN imagen_libro img ON img.id_imagen = img_min.id_imagen "
            +
            "WHERE (:titulo IS NULL OR l.titulo ILIKE CONCAT('%', :titulo, '%')) "
            +
            "AND (:autor IS NULL OR l.autor ILIKE CONCAT('%', :autor, '%')) "
            +
            "AND (:idCategoria IS NULL OR l.id_categoria = :idCategoria) "
            +
            "AND (:estado IS NULL OR l.estado = :estado) "
            +
            "ORDER BY l.fecha_registro DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<LibroHomeProjection> buscarLibros(
            @Param("titulo") String titulo,
            @Param("autor") String autor,
            @Param("idCategoria") Integer idCategoria,
            @Param("estado") String estado,
            @Param("limit") int limit,
            @Param("offset") int offset);

    @Modifying
    @Query(value = "INSERT INTO imagen_libro (id_libro, url_imagen) VALUES (:idLibro, :urlImagen)", nativeQuery = true)
    void vincularImagen(@Param("idLibro") Integer idLibro, @Param("urlImagen") String urlImagen);
}
