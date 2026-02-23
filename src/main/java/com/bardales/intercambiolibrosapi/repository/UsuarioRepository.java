package com.bardales.intercambiolibrosapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bardales.intercambiolibrosapi.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    @Query(value = "SELECT u.id_usuario AS idUsuario, u.nombres AS nombres, u.apellidos AS apellidos, "
            +
            "u.correo AS correo, u.password AS password, u.estado AS estado "
            +
            "FROM usuario u WHERE u.correo = :correo AND u.password = :password", nativeQuery = true)
    List<LoginUsuarioAppProjection> loginUsuarioApp(@Param("correo") String correo, @Param("password") String password);

    @Query(value = "SELECT COALESCE(AVG(r.puntuacion), 0) FROM resena r WHERE r.id_evaluado = :idUsuario", nativeQuery = true)
    Double obtenerValoracion(@Param("idUsuario") Integer idUsuario);

    @Modifying
    @Query(value = "UPDATE usuario SET nombres = :nombres, apellidos = :apellidos, url_foto_perfil = :urlFoto "
            +
            "WHERE id_usuario = :idUsuario", nativeQuery = true)
    int actualizarPerfil(
            @Param("idUsuario") Integer idUsuario,
            @Param("nombres") String nombres,
            @Param("apellidos") String apellidos,
            @Param("urlFoto") String urlFoto);
}
