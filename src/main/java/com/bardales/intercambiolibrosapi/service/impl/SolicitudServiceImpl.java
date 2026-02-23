package com.bardales.intercambiolibrosapi.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bardales.intercambiolibrosapi.dto.SolicitudCrearDTO;
import com.bardales.intercambiolibrosapi.dto.SolicitudResumenDTO;
import com.bardales.intercambiolibrosapi.repository.SolicitudRepository;
import com.bardales.intercambiolibrosapi.repository.SolicitudResumenProjection;
import com.bardales.intercambiolibrosapi.service.SolicitudService;

@Service
public class SolicitudServiceImpl implements SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final JdbcTemplate jdbcTemplate;

    public SolicitudServiceImpl(SolicitudRepository solicitudRepository, JdbcTemplate jdbcTemplate) {
        this.solicitudRepository = solicitudRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public Integer registrarSolicitud(SolicitudCrearDTO dto) {
        Integer idSolicitud = jdbcTemplate.queryForObject(
                "INSERT INTO solicitud (id_solicitante, id_receptor, tipo, estado) VALUES (?, ?, ?, 'pendiente') RETURNING id_solicitud",
                Integer.class,
                dto.getIdSolicitante(),
                dto.getIdReceptor(),
                dto.getTipo());

        if (dto.getIdLibro() != null) {
            solicitudRepository.vincularDetalle(idSolicitud, dto.getIdLibro(), "receptor");
        }

        return idSolicitud;
    }

    @Override
    public List<SolicitudResumenDTO> listarPorUsuario(int idUsuario) {
        return solicitudRepository.listarPorUsuario(idUsuario)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void actualizarEstado(int idSolicitud, String nuevoEstado) {
        solicitudRepository.actualizarEstado(idSolicitud, nuevoEstado);
    }

    private SolicitudResumenDTO toDto(SolicitudResumenProjection p) {
        return new SolicitudResumenDTO(
                p.getId_solicitud(),
                p.getTitulo_libro(),
                p.getNombre_contraparte(),
                p.getTipo(),
                p.getEstado(),
                p.getFecha_solicitud(),
                p.getUrl_imagen()
        );
    }
}
