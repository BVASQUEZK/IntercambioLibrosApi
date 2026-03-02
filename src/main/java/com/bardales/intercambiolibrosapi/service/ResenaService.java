package com.bardales.intercambiolibrosapi.service;

import com.bardales.intercambiolibrosapi.dto.ResenaCrearDTO;
import com.bardales.intercambiolibrosapi.dto.ResenaEstadoDTO;

public interface ResenaService {
    ResenaEstadoDTO obtenerEstadoResena(int idSolicitud, int idUsuario);
    void registrarResena(int idSolicitud, int idUsuario, ResenaCrearDTO dto);
}
