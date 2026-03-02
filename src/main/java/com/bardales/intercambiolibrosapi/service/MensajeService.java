package com.bardales.intercambiolibrosapi.service;

import java.util.List;

import com.bardales.intercambiolibrosapi.dto.MensajeDTO;
import com.bardales.intercambiolibrosapi.dto.MensajeEnviarDTO;

public interface MensajeService {
    List<MensajeDTO> listarMensajes(int idSolicitud, int idUsuario);
    void enviarMensaje(MensajeEnviarDTO dto, int idUsuarioAuth);
}
