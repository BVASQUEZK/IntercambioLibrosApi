package com.bardales.intercambiolibrosapi.dto;

public class ResenaEstadoDTO {
    private Integer idSolicitud;
    private boolean yaReseno;
    private boolean puedeResenar;

    public ResenaEstadoDTO() {
    }

    public ResenaEstadoDTO(Integer idSolicitud, boolean yaReseno, boolean puedeResenar) {
        this.idSolicitud = idSolicitud;
        this.yaReseno = yaReseno;
        this.puedeResenar = puedeResenar;
    }

    public Integer getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(Integer idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public boolean isYaReseno() {
        return yaReseno;
    }

    public void setYaReseno(boolean yaReseno) {
        this.yaReseno = yaReseno;
    }

    public boolean isPuedeResenar() {
        return puedeResenar;
    }

    public void setPuedeResenar(boolean puedeResenar) {
        this.puedeResenar = puedeResenar;
    }
}
