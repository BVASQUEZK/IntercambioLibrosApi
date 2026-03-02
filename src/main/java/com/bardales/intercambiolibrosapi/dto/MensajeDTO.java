package com.bardales.intercambiolibrosapi.dto;

import java.time.LocalDateTime;

public class MensajeDTO {
    private Integer idMensaje;
    private Integer idEmisor;
    private String contenido;
    private LocalDateTime fechaEnvio;
    private boolean esMio;
    private boolean leido;

    public MensajeDTO() {
    }

    public MensajeDTO(
            Integer idMensaje,
            Integer idEmisor,
            String contenido,
            LocalDateTime fechaEnvio,
            boolean esMio,
            boolean leido) {
        this.idMensaje = idMensaje;
        this.idEmisor = idEmisor;
        this.contenido = contenido;
        this.fechaEnvio = fechaEnvio;
        this.esMio = esMio;
        this.leido = leido;
    }

    public Integer getIdMensaje() {
        return idMensaje;
    }

    public void setIdMensaje(Integer idMensaje) {
        this.idMensaje = idMensaje;
    }

    public Integer getIdEmisor() {
        return idEmisor;
    }

    public void setIdEmisor(Integer idEmisor) {
        this.idEmisor = idEmisor;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public boolean isEsMio() {
        return esMio;
    }

    public void setEsMio(boolean esMio) {
        this.esMio = esMio;
    }

    public boolean isLeido() {
        return leido;
    }

    public void setLeido(boolean leido) {
        this.leido = leido;
    }
}
