package com.bardales.intercambiolibrosapi.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bardales.intercambiolibrosapi.dto.MensajeDTO;
import com.bardales.intercambiolibrosapi.dto.MensajeEnviarDTO;
import com.bardales.intercambiolibrosapi.exception.ForbiddenException;
import com.bardales.intercambiolibrosapi.repository.MensajeProjection;
import com.bardales.intercambiolibrosapi.repository.MensajeRepository;
import com.bardales.intercambiolibrosapi.service.MensajeService;

@Service
public class MensajeServiceImpl implements MensajeService {

    private final MensajeRepository mensajeRepository;

    public MensajeServiceImpl(MensajeRepository mensajeRepository) {
        this.mensajeRepository = mensajeRepository;
    }

    @Override
    @Transactional
    public List<MensajeDTO> listarMensajes(int idSolicitud, int idUsuario) {
        validarParticipante(idSolicitud, idUsuario);
        mensajeRepository.marcarMensajesLeidos(idSolicitud, idUsuario);
        return mensajeRepository.listarMensajes(idSolicitud)
                .stream()
                .map(p -> toDto(p, idUsuario))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void enviarMensaje(MensajeEnviarDTO dto, int idUsuarioAuth) {
        if (dto.getIdEmisor() == null || dto.getIdEmisor() != idUsuarioAuth) {
            throw new ForbiddenException("No autorizado para enviar mensaje con otro emisor");
        }
        validarParticipante(dto.getIdSolicitud(), idUsuarioAuth);
        mensajeRepository.enviarMensaje(dto.getIdSolicitud(), dto.getIdEmisor(), dto.getContenido());
    }

    private void validarParticipante(int idSolicitud, int idUsuario) {
        Integer existe = mensajeRepository.existeParticipante(idSolicitud, idUsuario);
        if (existe == null || existe == 0) {
            throw new ForbiddenException("No autorizado para acceder a esta solicitud");
        }
    }

    private MensajeDTO toDto(MensajeProjection p, int idUsuario) {
        boolean esMio = p.getId_emisor() != null && p.getId_emisor() == idUsuario;
        boolean leido = Boolean.TRUE.equals(p.getLeido());
        return new MensajeDTO(
                p.getId_mensaje(),
                p.getId_emisor(),
                p.getContenido(),
                p.getFecha_envio(),
                esMio,
                leido);
    }
}
