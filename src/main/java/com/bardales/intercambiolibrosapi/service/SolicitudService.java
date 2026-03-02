package com.bardales.intercambiolibrosapi.service;

import java.util.List;

import com.bardales.intercambiolibrosapi.dto.SolicitudCrearDTO;
import com.bardales.intercambiolibrosapi.dto.SolicitudResumenDTO;

public interface SolicitudService {
    Integer registrarSolicitud(SolicitudCrearDTO dto);
    List<SolicitudResumenDTO> listarPorUsuario(int idUsuario);
    void actualizarEstado(int idSolicitud, String nuevoEstado, int idUsuario);
}
