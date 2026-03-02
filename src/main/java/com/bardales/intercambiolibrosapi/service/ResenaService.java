package com.bardales.intercambiolibrosapi.service;

import com.bardales.intercambiolibrosapi.dto.ResenaCrearDTO;
import com.bardales.intercambiolibrosapi.dto.ResenaEstadoDTO;
import com.bardales.intercambiolibrosapi.dto.ResenaUsuarioDTO;

import java.util.List;

public interface ResenaService {
    ResenaEstadoDTO obtenerEstadoResena(int idSolicitud, int idUsuario);

    void registrarResena(int idSolicitud, int idUsuario, ResenaCrearDTO dto);

    List<ResenaUsuarioDTO> listarResenasPorUsuario(int idUsuario, Integer limit, Integer offset);
}
