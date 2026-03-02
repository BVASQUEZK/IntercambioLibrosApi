package com.bardales.intercambiolibrosapi.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public class LibroRegistroDTO {
    @NotBlank
    private String titulo;

    @NotBlank
    private String autor;

    private String descripcion;

    @NotBlank
    private String condicion;

    private String situacion;

    private String ubicacion;

    private Integer idCategoria;
    private List<Integer> idCategorias;

    @NotEmpty
    private List<String> urlsImagenes;

    public LibroRegistroDTO() {
    }

    public LibroRegistroDTO(String titulo, String autor, String descripcion, String condicion, String situacion, String ubicacion,
            Integer idCategoria, List<Integer> idCategorias, List<String> urlsImagenes) {
        this.titulo = titulo;
        this.autor = autor;
        this.descripcion = descripcion;
        this.condicion = condicion;
        this.situacion = situacion;
        this.ubicacion = ubicacion;
        this.idCategoria = idCategoria;
        this.idCategorias = idCategorias;
        this.urlsImagenes = urlsImagenes;
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

    public String getCondicion() {
        return condicion;
    }

    public void setCondicion(String condicion) {
        this.condicion = condicion;
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
