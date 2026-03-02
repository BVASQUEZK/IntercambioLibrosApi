package com.bardales.intercambiolibrosapi.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ResenaCrearDTO {

    @NotNull
    @Min(1)
    @Max(5)
    private Integer puntuacion;

    private String comentario;

    public ResenaCrearDTO() {
    }

    public ResenaCrearDTO(Integer puntuacion, String comentario) {
        this.puntuacion = puntuacion;
        this.comentario = comentario;
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
}
