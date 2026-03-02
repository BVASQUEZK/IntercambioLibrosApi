package com.bardales.intercambiolibrosapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bardales.intercambiolibrosapi.entity.Solicitud;

public interface SolicitudRepository extends JpaRepository<Solicitud, Integer> {

    @Modifying
    @Query(value = "INSERT INTO detalle_solicitud (id_solicitud, id_libro, propietario) VALUES (:idSolicitud, :idLibro, :propietario)", nativeQuery = true)
    void vincularDetalle(@Param("idSolicitud") Integer idSolicitud,
            @Param("idLibro") Integer idLibro,
            @Param("propietario") String propietario);

    @Modifying
    @Query(value = "UPDATE solicitud SET estado = :estado WHERE id_solicitud = :idSolicitud", nativeQuery = true)
    void actualizarEstado(@Param("idSolicitud") Integer idSolicitud, @Param("estado") String estado);

    @Query(value = "SELECT s.id_solicitud AS id_solicitud, "
            + "s.id_solicitante AS id_solicitante, "
            + "s.id_receptor AS id_receptor, "
            + "l.id_libro AS id_libro, "
            + "l.titulo AS titulo_libro, "
            + "CASE WHEN s.id_solicitante = :idUsuario "
            + "THEN CONCAT(ur.nombres, ' ', ur.apellidos) "
            + "ELSE CONCAT(us.nombres, ' ', us.apellidos) END AS nombre_contraparte, "
            + "s.tipo AS tipo, s.estado AS estado, s.fecha_solicitud AS fecha_solicitud, "
            + "img.url_imagen AS url_imagen "
            + "FROM solicitud s "
            + "INNER JOIN detalle_solicitud ds ON ds.id_solicitud = s.id_solicitud AND ds.propietario = 'receptor' "
            + "INNER JOIN libro l ON l.id_libro = ds.id_libro "
            + "INNER JOIN usuario us ON us.id_usuario = s.id_solicitante "
            + "INNER JOIN usuario ur ON ur.id_usuario = s.id_receptor "
            + "LEFT JOIN (SELECT id_libro, MIN(id_imagen) AS id_imagen FROM imagen_libro GROUP BY id_libro) img_min "
            + "ON img_min.id_libro = l.id_libro "
            + "LEFT JOIN imagen_libro img ON img.id_imagen = img_min.id_imagen "
            + "WHERE s.id_solicitante = :idUsuario OR s.id_receptor = :idUsuario "
            + "ORDER BY s.fecha_solicitud DESC",
            nativeQuery = true)
    List<SolicitudResumenProjection> listarPorUsuario(@Param("idUsuario") Integer idUsuario);
}
