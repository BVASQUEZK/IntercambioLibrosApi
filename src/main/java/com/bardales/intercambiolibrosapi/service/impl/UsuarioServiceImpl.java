package com.bardales.intercambiolibrosapi.service.impl;

import com.bardales.intercambiolibrosapi.dto.LoginResponseDTO;
import com.bardales.intercambiolibrosapi.dto.PerfilUsuarioDTO;
import com.bardales.intercambiolibrosapi.dto.UsuarioUpdateDTO;
import com.bardales.intercambiolibrosapi.entity.Usuario;
import com.bardales.intercambiolibrosapi.exception.ForbiddenException;
import com.bardales.intercambiolibrosapi.exception.ResourceNotFoundException;
import com.bardales.intercambiolibrosapi.exception.UnauthorizedException;
import com.bardales.intercambiolibrosapi.repository.UsuarioRepository;
import com.bardales.intercambiolibrosapi.service.UsuarioService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioServiceImpl implements UsuarioService {
    private static final int MAX_FOTO_CHARS = 3_000_000;

    private final UsuarioRepository usuarioRepository;
    private final JdbcTemplate jdbcTemplate;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, JdbcTemplate jdbcTemplate) {
        this.usuarioRepository = usuarioRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public LoginResponseDTO login(String correo, String password) {
        List<com.bardales.intercambiolibrosapi.repository.LoginUsuarioAppProjection> rows =
                usuarioRepository.loginUsuarioApp(correo, password);
        if (rows == null || rows.isEmpty()) {
            throw new UnauthorizedException("Credenciales invalidas");
        }
        var row = rows.get(0);
        Integer idUsuario = row.getIdUsuario();
        String nombres = row.getNombres();
        String apellidos = row.getApellidos();
        String estado = row.getEstado();
        if (estado != null && !"activo".equalsIgnoreCase(estado)) {
            throw new ForbiddenException("Usuario suspendido");
        }
        String nombre = null;
        if (nombres != null && apellidos != null) {
            nombre = (nombres + " " + apellidos).trim();
        } else if (nombres != null) {
            nombre = nombres;
        } else if (apellidos != null) {
            nombre = apellidos;
        }
        String token = UUID.randomUUID().toString();
        return new LoginResponseDTO(idUsuario == null ? null : idUsuario.intValue(), nombre, token);
    }

    @Override
    public PerfilUsuarioDTO obtenerPerfil(int idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Double promedio = usuarioRepository.obtenerValoracion(idUsuario);
        return new PerfilUsuarioDTO(
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getUrlFotoPerfil(),
                usuario.getFechaRegistro(),
                promedio == null ? 0.0 : promedio);
    }

    @Override
    @Transactional
    public LoginResponseDTO actualizarPerfil(int idUsuarioHeader, UsuarioUpdateDTO dto) {
        if (dto.getIdUsuario() == null || !dto.getIdUsuario().equals(idUsuarioHeader)) {
            throw new UnauthorizedException("No autorizado para editar este perfil");
        }
        if (dto.getUrlFoto() != null && dto.getUrlFoto().length() > MAX_FOTO_CHARS) {
            throw new ResourceNotFoundException("La imagen de perfil es demasiado grande");
        }

        usuarioRepository.actualizarPerfil(
                dto.getIdUsuario(),
                dto.getNombres(),
                dto.getApellidos(),
                dto.getUrlFoto()
        );

        Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        String urlFoto = usuario.getUrlFotoPerfil();
        if (urlFoto == null || urlFoto.isBlank()) {
            urlFoto = "default_user.png";
        }
        return new LoginResponseDTO(usuario.getNombres(), usuario.getApellidos(), urlFoto);
    }

    @Override
    public Map<String, Object> registrarUsuario(String nombres, String apellidos, String correo, String clave, String dni) {
        try {
            Integer idUsuario = jdbcTemplate.queryForObject(
                    "INSERT INTO usuario (nombres, apellidos, correo, password, dni) VALUES (?, ?, ?, ?, ?) RETURNING id_usuario",
                    Integer.class,
                    nombres, apellidos, correo, clave, dni);

            if (idUsuario == null || idUsuario <= 0) {
                throw new ResourceNotFoundException("No se pudo registrar el usuario");
            }
            return Map.of("mensaje", "REGISTRO_OK", "id_usuario", idUsuario);
        } catch (DataIntegrityViolationException ex) {
            throw new ResourceNotFoundException("Correo o DNI ya registrado");
        }
    }
}
