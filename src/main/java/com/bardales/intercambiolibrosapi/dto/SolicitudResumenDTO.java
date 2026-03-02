package com.bardales.intercambiolibrosapi.dto;

import java.time.LocalDateTime;

public class SolicitudResumenDTO {
    private Integer idSolicitud;
    private Integer idSolicitante;
    private Integer idReceptor;
    private Integer idLibro;
    private String tituloLibro;
    private String nombreContraparte;
    private String tipo;
    private String estado;
    private LocalDateTime fechaSolicitud;
    private String urlImagen;

    public SolicitudResumenDTO() {
    }

    public SolicitudResumenDTO(Integer idSolicitud, Integer idSolicitante, Integer idReceptor, Integer idLibro,
            String tituloLibro, String nombreContraparte, String tipo,
            String estado, LocalDateTime fechaSolicitud, String urlImagen) {
        this.idSolicitud = idSolicitud;
        this.idSolicitante = idSolicitante;
        this.idReceptor = idReceptor;
        this.idLibro = idLibro;
        this.tituloLibro = tituloLibro;
        this.nombreContraparte = nombreContraparte;
        this.tipo = tipo;
        this.estado = estado;
        this.fechaSolicitud = fechaSolicitud;
        this.urlImagen = urlImagen;
    }

    public Integer getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(Integer idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public Integer getIdSolicitante() {
        return idSolicitante;
    }

    public void setIdSolicitante(Integer idSolicitante) {
        this.idSolicitante = idSolicitante;
    }

    public Integer getIdReceptor() {
        return idReceptor;
    }

    public void setIdReceptor(Integer idReceptor) {
        this.idReceptor = idReceptor;
    }

    public Integer getIdLibro() {
        return idLibro;
    }

    public void setIdLibro(Integer idLibro) {
        this.idLibro = idLibro;
    }

    public String getTituloLibro() {
        return tituloLibro;
    }

    public void setTituloLibro(String tituloLibro) {
        this.tituloLibro = tituloLibro;
    }

    public String getNombreContraparte() {
        return nombreContraparte;
    }

    public void setNombreContraparte(String nombreContraparte) {
        this.nombreContraparte = nombreContraparte;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public String getUrlImagen() {
        return urlImagen;
    }

    public void setUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }
}
