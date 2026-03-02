package com.bardales.intercambiolibrosapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bardales.intercambiolibrosapi.entity.Mensaje;

public interface MensajeRepository extends JpaRepository<Mensaje, Integer> {

    @Query(value = "SELECT m.id_mensaje AS id_mensaje, m.id_emisor AS id_emisor, "
            +
            "m.contenido AS contenido, m.fecha_envio AS fecha_envio "
            +
            "FROM mensaje m WHERE m.id_solicitud = :idSolicitud ORDER BY m.fecha_envio ASC", nativeQuery = true)
    List<MensajeProjection> listarMensajes(@Param("idSolicitud") Integer idSolicitud);

    @Modifying
    @Query(value = "INSERT INTO mensaje (id_solicitud, id_emisor, contenido) VALUES (:idSolicitud, :idEmisor, :contenido)", nativeQuery = true)
    void enviarMensaje(@Param("idSolicitud") Integer idSolicitud,
            @Param("idEmisor") Integer idEmisor,
            @Param("contenido") String contenido);

    @Query(value = "SELECT COUNT(1) FROM solicitud s "
            + "WHERE s.id_solicitud = :idSolicitud "
            + "AND (s.id_solicitante = :idUsuario OR s.id_receptor = :idUsuario)", nativeQuery = true)
    Integer existeParticipante(@Param("idSolicitud") Integer idSolicitud, @Param("idUsuario") Integer idUsuario);
}
