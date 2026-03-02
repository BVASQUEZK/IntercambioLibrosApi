package com.bardales.intercambiolibrosapi.dto;

import java.util.List;

public class LibroCreadoDTO {
    private Integer idLibro;
    private Integer idUsuario;
    private Integer idCategoria;
    private List<Integer> idCategorias;
    private String titulo;
    private String autor;
    private String descripcion;
    private String estado;
    private String condicion;
    private String situacion;
    private String ubicacion;
    private List<String> urlsImagenes;

    public LibroCreadoDTO() {
    }

    public LibroCreadoDTO(Integer idLibro, Integer idUsuario, Integer idCategoria, List<Integer> idCategorias, String titulo, String autor,
            String descripcion, String estado, String condicion, String situacion, String ubicacion, List<String> urlsImagenes) {
        this.idLibro = idLibro;
        this.idUsuario = idUsuario;
        this.idCategoria = idCategoria;
        this.idCategorias = idCategorias;
        this.titulo = titulo;
        this.autor = autor;
        this.descripcion = descripcion;
        this.estado = estado;
        this.condicion = condicion;
        this.situacion = situacion;
        this.ubicacion = ubicacion;
        this.urlsImagenes = urlsImagenes;
    }

    public Integer getIdLibro() {
        return idLibro;
    }

    public void setIdLibro(Integer idLibro) {
        this.idLibro = idLibro;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
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

    public List<String> getUrlsImagenes() {
        return urlsImagenes;
    }

    public void setUrlsImagenes(List<String> urlsImagenes) {
        this.urlsImagenes = urlsImagenes;
    }
}
