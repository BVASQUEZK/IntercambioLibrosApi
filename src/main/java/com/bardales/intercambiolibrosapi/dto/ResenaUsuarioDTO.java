package com.bardales.intercambiolibrosapi.dto;

import java.time.LocalDateTime;

public class ResenaUsuarioDTO {
    private Integer idResena;
    private Integer idSolicitud;
    private Integer idEvaluador;
    private String evaluadorNombre;
    private String evaluadorFoto;
    private Integer puntuacion;
    private String comentario;
    private LocalDateTime fecha;

    public ResenaUsuarioDTO() {
    }

    public ResenaUsuarioDTO(
            Integer idResena,
            Integer idSolicitud,
            Integer idEvaluador,
            String evaluadorNombre,
            String evaluadorFoto,
            Integer puntuacion,
            String comentario,
            LocalDateTime fecha) {
        this.idResena = idResena;
        this.idSolicitud = idSolicitud;
        this.idEvaluador = idEvaluador;
        this.evaluadorNombre = evaluadorNombre;
        this.evaluadorFoto = evaluadorFoto;
        this.puntuacion = puntuacion;
        this.comentario = comentario;
        this.fecha = fecha;
    }

    public Integer getIdResena() {
        return idResena;
    }

    public void setIdResena(Integer idResena) {
        this.idResena = idResena;
    }

    public Integer getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(Integer idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public Integer getIdEvaluador() {
        return idEvaluador;
    }

    public void setIdEvaluador(Integer idEvaluador) {
        this.idEvaluador = idEvaluador;
    }

    public String getEvaluadorNombre() {
        return evaluadorNombre;
    }

    public void setEvaluadorNombre(String evaluadorNombre) {
        this.evaluadorNombre = evaluadorNombre;
    }

    public String getEvaluadorFoto() {
        return evaluadorFoto;
    }

    public void setEvaluadorFoto(String evaluadorFoto) {
        this.evaluadorFoto = evaluadorFoto;
    }

    public Integer getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(Integer puntuacion) {
        this.puntuacion = puntuacion;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
