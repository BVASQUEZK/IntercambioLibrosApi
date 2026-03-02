package com.bardales.intercambiolibrosapi.dto;

import java.time.LocalDateTime;

public class PerfilUsuarioDTO {
    private String nombres;
    private String apellidos;
    private String urlFotoPerfil;
    private LocalDateTime fechaRegistro;
    private Double promedioPuntuacion;
    private Double latitud;
    private Double longitud;
    private String distrito;
    private String departamento;
    private Integer puntos;

    public PerfilUsuarioDTO() {
    }

    public PerfilUsuarioDTO(String nombres, String apellidos, String urlFotoPerfil, LocalDateTime fechaRegistro,
            Double promedioPuntuacion) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.urlFotoPerfil = urlFotoPerfil;
        this.fechaRegistro = fechaRegistro;
        this.promedioPuntuacion = promedioPuntuacion;
        this.puntos = 0;
    }

    public PerfilUsuarioDTO(
            String nombres,
            String apellidos,
            String urlFotoPerfil,
            LocalDateTime fechaRegistro,
            Double promedioPuntuacion,
            Double latitud,
            Double longitud,
            String distrito,
            String departamento,
            Integer puntos) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.urlFotoPerfil = urlFotoPerfil;
        this.fechaRegistro = fechaRegistro;
        this.promedioPuntuacion = promedioPuntuacion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.distrito = distrito;
        this.departamento = departamento;
        this.puntos = puntos;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getUrlFotoPerfil() {
        return urlFotoPerfil;
    }

    public void setUrlFotoPerfil(String urlFotoPerfil) {
        this.urlFotoPerfil = urlFotoPerfil;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Double getPromedioPuntuacion() {
        return promedioPuntuacion;
    }

    public void setPromedioPuntuacion(Double promedioPuntuacion) {
        this.promedioPuntuacion = promedioPuntuacion;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public Integer getPuntos() {
        return puntos;
    }

    public void setPuntos(Integer puntos) {
        this.puntos = puntos;
    }
}
