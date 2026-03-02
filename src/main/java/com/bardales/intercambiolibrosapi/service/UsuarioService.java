package com.bardales.intercambiolibrosapi.service;

import com.bardales.intercambiolibrosapi.dto.LoginResponseDTO;
import com.bardales.intercambiolibrosapi.dto.PerfilUsuarioDTO;
import com.bardales.intercambiolibrosapi.dto.UsuarioUpdateDTO;
import java.util.Map;

public interface UsuarioService {
    LoginResponseDTO login(String correo, String password);
    PerfilUsuarioDTO obtenerPerfil(int idUsuario);
    LoginResponseDTO actualizarPerfil(int idUsuarioHeader, UsuarioUpdateDTO dto);
    Integer sumarPuntoPorAnuncio(int idUsuarioHeader);
    Map<String, Object> registrarUsuario(String nombres, String apellidos, String correo, String clave, String dni);
}
