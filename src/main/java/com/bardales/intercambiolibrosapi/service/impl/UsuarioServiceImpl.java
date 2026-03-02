package com.bardales.intercambiolibrosapi.service.impl;

import com.bardales.intercambiolibrosapi.dto.LoginResponseDTO;
import com.bardales.intercambiolibrosapi.dto.PerfilUsuarioDTO;
import com.bardales.intercambiolibrosapi.dto.UsuarioUpdateDTO;
import com.bardales.intercambiolibrosapi.entity.Usuario;
import com.bardales.intercambiolibrosapi.exception.ForbiddenException;
import com.bardales.intercambiolibrosapi.exception.ResourceNotFoundException;
import com.bardales.intercambiolibrosapi.exception.UnauthorizedException;
import com.bardales.intercambiolibrosapi.repository.UsuarioRepository;
import com.bardales.intercambiolibrosapi.security.JwtService;
import com.bardales.intercambiolibrosapi.service.UsuarioService;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioServiceImpl implements UsuarioService {
    private static final int MAX_FOTO_CHARS = 3_000_000;

    private final UsuarioRepository usuarioRepository;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostConstruct
    void inicializarPuntosUsuario() {
        try {
            jdbcTemplate.execute("ALTER TABLE IF EXISTS usuario ADD COLUMN IF NOT EXISTS puntos INT");
            jdbcTemplate.execute("ALTER TABLE IF EXISTS usuario ALTER COLUMN puntos SET DEFAULT 1");
            jdbcTemplate.update("UPDATE usuario SET puntos = 1 WHERE puntos IS NULL OR puntos < 0");
        } catch (Exception ignored) {
            // Si falla este ajuste, el flujo normal devolvera error y se corrige en despliegue DB.
        }
    }

    @Override
    @Transactional
    public LoginResponseDTO login(String correo, String password) {
        String correoNormalizado = correo == null ? "" : correo.trim();
        if (correoNormalizado.isBlank() || password == null || password.isBlank()) {
            throw new UnauthorizedException("Credenciales invalidas");
        }
        List<com.bardales.intercambiolibrosapi.repository.LoginUsuarioAppProjection> rows =
                usuarioRepository.loginUsuarioAppByCorreo(correoNormalizado);
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
        if (idUsuario == null || idUsuario <= 0) {
            throw new UnauthorizedException("Credenciales invalidas");
        }
        String storedPassword = row.getPassword();
        if (storedPassword == null || storedPassword.isBlank()) {
            throw new UnauthorizedException("Credenciales invalidas");
        }
        boolean passwordOk = isBcryptHash(storedPassword)
                ? passwordEncoder.matches(password, storedPassword)
                : Objects.equals(storedPassword, password);
        if (!passwordOk) {
            throw new UnauthorizedException("Credenciales invalidas");
        }
        if (!isBcryptHash(storedPassword) && idUsuario != null) {
            usuarioRepository.actualizarPassword(idUsuario, passwordEncoder.encode(password));
        }

        String nombre = null;
        if (nombres != null && apellidos != null) {
            nombre = (nombres + " " + apellidos).trim();
        } else if (nombres != null) {
            nombre = nombres;
        } else if (apellidos != null) {
            nombre = apellidos;
        }
        String token = jwtService.generateToken(idUsuario.intValue());
        return new LoginResponseDTO(idUsuario.intValue(), nombre, token);
    }

    @Override
    public PerfilUsuarioDTO obtenerPerfil(int idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Double promedio = usuarioRepository.obtenerValoracion(idUsuario);
        Map<String, Object> ubicacion = obtenerUbicacionPerfil(idUsuario);
        return new PerfilUsuarioDTO(
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getUrlFotoPerfil(),
                usuario.getFechaRegistro(),
                promedio == null ? 0.0 : promedio,
                toDouble(ubicacion.get("latitud")),
                toDouble(ubicacion.get("longitud")),
                toStringOrNull(ubicacion.get("distrito")),
                toStringOrNull(ubicacion.get("departamento")),
                usuario.getPuntos() == null ? 0 : usuario.getPuntos());
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

        boolean shouldUpdateUbicacion = dto.getLatitud() != null
                || dto.getLongitud() != null
                || notBlank(dto.getDistrito())
                || notBlank(dto.getDepartamento());
        if (shouldUpdateUbicacion) {
            upsertUbicacion(
                    dto.getIdUsuario(),
                    dto.getLatitud(),
                    dto.getLongitud(),
                    normalizeBlank(dto.getDistrito()),
                    normalizeBlank(dto.getDepartamento()));
        }

        Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        String urlFoto = usuario.getUrlFotoPerfil();
        if (urlFoto == null || urlFoto.isBlank()) {
            urlFoto = "default_user.png";
        }
        return new LoginResponseDTO(usuario.getNombres(), usuario.getApellidos(), urlFoto);
    }

    @Override
    @Transactional
    public Integer sumarPuntoPorAnuncio(int idUsuarioHeader) {
        int updated = jdbcTemplate.update(
                "UPDATE usuario SET puntos = COALESCE(puntos, 0) + 1 WHERE id_usuario = ?",
                idUsuarioHeader);
        if (updated == 0) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        Integer puntos = jdbcTemplate.queryForObject(
                "SELECT COALESCE(puntos, 0) FROM usuario WHERE id_usuario = ?",
                Integer.class,
                idUsuarioHeader);
        return puntos == null ? 0 : puntos;
    }

    private Map<String, Object> obtenerUbicacionPerfil(int idUsuario) {
        return jdbcTemplate.query(
                "SELECT latitud AS latitud, longitud AS longitud, "
                        + "direccion AS distrito, ciudad AS departamento "
                        + "FROM ubicacion WHERE id_usuario = ? "
                        + "ORDER BY fecha_actualizacion DESC NULLS LAST, id_ubicacion DESC LIMIT 1",
                rs -> {
                    if (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        row.put("latitud", rs.getObject("latitud"));
                        row.put("longitud", rs.getObject("longitud"));
                        row.put("distrito", rs.getString("distrito"));
                        row.put("departamento", rs.getString("departamento"));
                        return row;
                    }
                    return Map.of();
                },
                idUsuario);
    }

    private void upsertUbicacion(
            int idUsuario,
            Double latitud,
            Double longitud,
            String distrito,
            String departamento) {
        Integer idUbicacion = jdbcTemplate.query(
                "SELECT id_ubicacion FROM ubicacion WHERE id_usuario = ? "
                        + "ORDER BY fecha_actualizacion DESC NULLS LAST, id_ubicacion DESC LIMIT 1",
                rs -> rs.next() ? rs.getInt(1) : null,
                idUsuario);

        if (idUbicacion == null) {
            jdbcTemplate.update(
                    "INSERT INTO ubicacion (id_usuario, latitud, longitud, direccion, ciudad, fecha_actualizacion) "
                            + "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)",
                    idUsuario, latitud, longitud, distrito, departamento);
        } else {
            jdbcTemplate.update(
                    "UPDATE ubicacion SET latitud = ?, longitud = ?, direccion = ?, ciudad = ?, "
                            + "fecha_actualizacion = CURRENT_TIMESTAMP WHERE id_ubicacion = ?",
                    latitud, longitud, distrito, departamento, idUbicacion);
        }
    }

    private boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String normalizeBlank(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Double toDouble(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(Objects.toString(raw));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String toStringOrNull(Object raw) {
        if (raw == null) {
            return null;
        }
        String value = Objects.toString(raw, "").trim();
        return value.isEmpty() ? null : value;
    }

    @Override
    public Map<String, Object> registrarUsuario(String nombres, String apellidos, String correo, String clave, String dni) {
        try {
            String correoNormalizado = correo == null ? null : correo.trim().toLowerCase();
            String passwordHash = passwordEncoder.encode(clave);
            Integer idUsuario = jdbcTemplate.queryForObject(
                    "INSERT INTO usuario (nombres, apellidos, correo, password, dni, puntos) VALUES (?, ?, ?, ?, ?, 1) RETURNING id_usuario",
                    Integer.class,
                    nombres, apellidos, correoNormalizado, passwordHash, dni);

            if (idUsuario == null || idUsuario <= 0) {
                throw new ResourceNotFoundException("No se pudo registrar el usuario");
            }
            return Map.of("mensaje", "REGISTRO_OK", "id_usuario", idUsuario);
        } catch (DataIntegrityViolationException ex) {
            throw new ResourceNotFoundException("Correo o DNI ya registrado");
        }
    }

    private boolean isBcryptHash(String password) {
        return password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$");
    }
}
