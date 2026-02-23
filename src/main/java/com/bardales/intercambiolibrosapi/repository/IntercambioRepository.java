package com.bardales.intercambiolibrosapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IntercambioRepository {

    @Query(value = "SELECT l.id_usuario FROM solicitud s " +
            "INNER JOIN detalle_solicitud ds ON ds.id_solicitud = s.id_solicitud AND ds.propietario = 'receptor' " +
            "INNER JOIN libro l ON l.id_libro = ds.id_libro " +
            "WHERE s.id_solicitud = :idSolicitud", nativeQuery = true)
    Integer obtenerIdDuenoLibro(@Param("idSolicitud") Integer idSolicitud);

    @Query(value = "SELECT s.id_solicitud AS id_solicitud, "
            +
            "l.titulo AS titulo_libro, "
            +
            "CASE WHEN :tipo = 'RECIBIDAS' THEN CONCAT(us.nombres, ' ', us.apellidos) "
            +
            "ELSE CONCAT(ur.nombres, ' ', ur.apellidos) END AS nombre_otra_parte, "
            +
            "s.estado AS estado, s.fecha_solicitud AS fecha_solicitud "
            +
            "FROM solicitud s "
            +
            "INNER JOIN detalle_solicitud ds ON ds.id_solicitud = s.id_solicitud AND ds.propietario = 'receptor' "
            +
            "INNER JOIN libro l ON l.id_libro = ds.id_libro "
            +
            "INNER JOIN usuario us ON us.id_usuario = s.id_solicitante "
            +
            "INNER JOIN usuario ur ON ur.id_usuario = s.id_receptor "
            +
            "WHERE (:tipo = 'RECIBIDAS' AND s.id_receptor = :idUsuario) "
            +
            "OR (:tipo = 'ENVIADAS' AND s.id_solicitante = :idUsuario) "
            +
            "ORDER BY s.fecha_solicitud DESC", nativeQuery = true)
    List<IntercambioSolicitudProjection> listarSolicitudes(
            @Param("idUsuario") Integer idUsuario,
            @Param("tipo") String tipo);

    @Query(value = "SELECT CONCAT(u.nombres, ' ', u.apellidos) AS emisor_nombre, "
            +
            "m.contenido AS contenido_mensaje, m.fecha_envio AS fecha_envio, "
            +
            "(m.id_emisor = :idUsuario) AS es_mio "
            +
            "FROM mensaje m INNER JOIN usuario u ON u.id_usuario = m.id_emisor "
            +
            "WHERE m.id_solicitud = :idSolicitud ORDER BY m.fecha_envio ASC", nativeQuery = true)
    List<IntercambioMensajeProjection> listarMensajesIntercambio(
            @Param("idSolicitud") Integer idSolicitud,
            @Param("idUsuario") Integer idUsuario);

    @Query(value = "SELECT COUNT(1) FROM solicitud s " +
            "WHERE s.id_solicitud = :idSolicitud " +
            "AND (s.id_solicitante = :idUsuario OR s.id_receptor = :idUsuario)", nativeQuery = true)
    Integer existeUsuarioEnSolicitud(@Param("idSolicitud") Integer idSolicitud, @Param("idUsuario") Integer idUsuario);
}
