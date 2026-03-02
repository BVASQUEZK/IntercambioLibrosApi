package com.bardales.intercambiolibrosapi.service.impl;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bardales.intercambiolibrosapi.dto.ResenaCrearDTO;
import com.bardales.intercambiolibrosapi.dto.ResenaEstadoDTO;
import com.bardales.intercambiolibrosapi.dto.ResenaUsuarioDTO;
import com.bardales.intercambiolibrosapi.exception.ForbiddenException;
import com.bardales.intercambiolibrosapi.exception.ResourceNotFoundException;
import com.bardales.intercambiolibrosapi.exception.UnauthorizedException;
import com.bardales.intercambiolibrosapi.service.ResenaService;

@Service
public class ResenaServiceImpl implements ResenaService {

    private final JdbcTemplate jdbcTemplate;

    public ResenaServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ResenaEstadoDTO obtenerEstadoResena(int idSolicitud, int idUsuario) {
        SolicitudContext solicitud = obtenerSolicitudYValidarParticipante(idSolicitud, idUsuario);
        boolean yaReseno = yaReseno(idSolicitud, idUsuario);
        boolean puedeResenar = "finalizado".equals(solicitud.estado) && !yaReseno;
        return new ResenaEstadoDTO(idSolicitud, yaReseno, puedeResenar);
    }

    @Override
    public List<ResenaUsuarioDTO> listarResenasPorUsuario(int idUsuario, Integer limit, Integer offset) {
        int safeLimit = limit == null ? 0 : Math.max(0, Math.min(limit, 100));
        int safeOffset = offset == null ? 0 : Math.max(0, offset);

        StringBuilder sql = new StringBuilder(
                "SELECT r.id_resena, r.id_solicitud, r.id_evaluador, "
                        + "TRIM(COALESCE(u.nombres, '') || ' ' || COALESCE(u.apellidos, '')) AS evaluador_nombre, "
                        + "u.url_foto_perfil AS evaluador_foto, r.puntuacion, r.comentario, r.fecha "
                        + "FROM resena r "
                        + "INNER JOIN usuario u ON u.id_usuario = r.id_evaluador "
                        + "WHERE r.id_evaluado = ? "
                        + "ORDER BY r.fecha DESC");

        List<Object> params = new ArrayList<>();
        params.add(idUsuario);

        if (safeLimit > 0) {
            sql.append(" LIMIT ?");
            params.add(safeLimit);
        }
        if (safeOffset > 0) {
            sql.append(" OFFSET ?");
            params.add(safeOffset);
        }

        return jdbcTemplate.query(
                sql.toString(),
                (rs, rowNum) -> new ResenaUsuarioDTO(
                        rs.getInt("id_resena"),
                        rs.getInt("id_solicitud"),
                        rs.getInt("id_evaluador"),
                        rs.getString("evaluador_nombre"),
                        rs.getString("evaluador_foto"),
                        rs.getInt("puntuacion"),
                        rs.getString("comentario"),
                        rs.getTimestamp("fecha") == null ? null : rs.getTimestamp("fecha").toLocalDateTime()),
                params.toArray());
    }

    @Override
    @Transactional
    public void registrarResena(int idSolicitud, int idUsuario, ResenaCrearDTO dto) {
        SolicitudContext solicitud = obtenerSolicitudYValidarParticipante(idSolicitud, idUsuario);
        if (!"finalizado".equals(solicitud.estado)) {
            throw new UnauthorizedException("Solo se puede dejar resena cuando el intercambio este finalizado");
        }

        int idEvaluado = solicitud.idSolicitante == idUsuario ? solicitud.idReceptor : solicitud.idSolicitante;
        int insertados = jdbcTemplate.update(
                "INSERT INTO resena (id_solicitud, id_evaluador, id_evaluado, puntuacion, comentario) "
                        + "SELECT ?, ?, ?, ?, NULLIF(TRIM(?), '') "
                        + "WHERE NOT EXISTS (SELECT 1 FROM resena WHERE id_solicitud = ? AND id_evaluador = ?)",
                idSolicitud,
                idUsuario,
                idEvaluado,
                dto.getPuntuacion(),
                dto.getComentario(),
                idSolicitud,
                idUsuario);

        if (insertados == 0) {
            throw new UnauthorizedException("Ya dejaste una resena para esta solicitud");
        }
    }

    private boolean yaReseno(int idSolicitud, int idUsuario) {
        Integer total = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM resena WHERE id_solicitud = ? AND id_evaluador = ?",
                Integer.class,
                idSolicitud,
                idUsuario);
        return total != null && total > 0;
    }

    private SolicitudContext obtenerSolicitudYValidarParticipante(int idSolicitud, int idUsuario) {
        Map<String, Object> solicitud;
        try {
            solicitud = jdbcTemplate.queryForMap(
                    "SELECT id_solicitante, id_receptor, estado FROM solicitud WHERE id_solicitud = ?",
                    idSolicitud);
        } catch (EmptyResultDataAccessException ex) {
            throw new ResourceNotFoundException("Solicitud no encontrada");
        }

        int idSolicitante = ((Number) solicitud.get("id_solicitante")).intValue();
        int idReceptor = ((Number) solicitud.get("id_receptor")).intValue();
        String estado = String.valueOf(solicitud.get("estado")).trim().toLowerCase();

        boolean esParticipante = idUsuario == idSolicitante || idUsuario == idReceptor;
        if (!esParticipante) {
            throw new ForbiddenException("No autorizado para registrar resena en esta solicitud");
        }

        return new SolicitudContext(idSolicitante, idReceptor, estado);
    }

    private static class SolicitudContext {
        private final int idSolicitante;
        private final int idReceptor;
        private final String estado;

        private SolicitudContext(int idSolicitante, int idReceptor, String estado) {
            this.idSolicitante = idSolicitante;
            this.idReceptor = idReceptor;
            this.estado = estado;
        }
    }
}
