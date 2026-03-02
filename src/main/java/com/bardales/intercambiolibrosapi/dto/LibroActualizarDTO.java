package com.bardales.intercambiolibrosapi.dto;

import java.util.List;

public class LibroActualizarDTO {
    private String titulo;
    private String autor;
    private String descripcion;
    private String estado;
    private String situacion;
    private String ubicacion;
    private Integer idCategoria;
    private List<Integer> idCategorias;
    private List<String> urlsImagenes;

    public LibroActualizarDTO() {
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getSituacion() {
        return situacion;
    }

    public void setSituacion(String situacion) {
        this.situacion = situacion;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public Integer getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }

    public List<Integer> getIdCategorias() {
        return idCategorias;
    }

    public void setIdCategorias(List<Integer> idCategorias) {
        this.idCategorias = idCategorias;
    }

    public List<String> getUrlsImagenes() {
        return urlsImagenes;
    }

    public void setUrlsImagenes(List<String> urlsImagenes) {
        this.urlsImagenes = urlsImagenes;
    }
}
