package com.bardales.intercambiolibrosapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class LibroDTO {
    @NotNull
    private Integer idLibro;

    @NotBlank
    private String titulo;

    @NotBlank
    private String autor;

    @NotBlank
    private String urlPortada;

    private Integer idUsuario;
    private String distrito;
    private String departamento;

    public LibroDTO() {
    }

    public LibroDTO(Integer idLibro, String titulo, String autor, String urlPortada) {
        this.idLibro = idLibro;
        this.titulo = titulo;
        this.autor = autor;
        this.urlPortada = urlPortada;
    }

    public LibroDTO(
            Integer idLibro,
            String titulo,
            String autor,
            String urlPortada,
            Integer idUsuario,
            String distrito,
            String departamento) {
        this.idLibro = idLibro;
        this.titulo = titulo;
        this.autor = autor;
        this.urlPortada = urlPortada;
        this.idUsuario = idUsuario;
        this.distrito = distrito;
        this.departamento = departamento;
    }

    public Integer getIdLibro() {
        return idLibro;
    }

    public void setIdLibro(Integer idLibro) {
        this.idLibro = idLibro;
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

    public String getUrlPortada() {
        return urlPortada;
    }

    public void setUrlPortada(String urlPortada) {
        this.urlPortada = urlPortada;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
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
}
