package com.bardales.intercambiolibrosapi.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "libro")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_libro")
    private Integer idLibro;

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @Column(name = "id_categoria")
    private Integer idCategoria;

    @JsonBackReference(value = "categoria-libro")
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "libro_categoria",
            joinColumns = @jakarta.persistence.JoinColumn(name = "id_libro"),
            inverseJoinColumns = @jakarta.persistence.JoinColumn(name = "id_categoria")
    )
    private List<Categoria> categorias = new ArrayList<>();

    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @Column(name = "autor", length = 150)
    private String autor;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "estado")
    private String estado;

    @Column(name = "condicion")
    private String condicion;

    @Column(name = "situacion")
    private String situacion;

    @Column(name = "ubicacion")
    private String ubicacion;

    @Column(name = "disponible")
    private Boolean disponible;

    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;

    @OneToMany(mappedBy = "libro", fetch = FetchType.LAZY)
    @OrderBy("idImagen ASC")
    private List<ImagenLibro> imagenes = new ArrayList<>();

    public Libro() {
    }

    public Libro(Integer idLibro, Integer idUsuario, Integer idCategoria, List<Categoria> categorias, String titulo, String autor,
            String descripcion, String estado, String condicion, String situacion, String ubicacion,
            Boolean disponible, LocalDateTime fechaRegistro, List<ImagenLibro> imagenes) {
        this.idLibro = idLibro;
        this.idUsuario = idUsuario;
        this.idCategoria = idCategoria;
        this.categorias = categorias;
        this.titulo = titulo;
        this.autor = autor;
        this.descripcion = descripcion;
        this.estado = estado;
        this.condicion = condicion;
        this.situacion = situacion;
        this.ubicacion = ubicacion;
        this.disponible = disponible;
        this.fechaRegistro = fechaRegistro;
        this.imagenes = imagenes;
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

    public List<Categoria> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<Categoria> categorias) {
        this.categorias = categorias;
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

    public Boolean getDisponible() {
        return disponible;
    }

    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public List<ImagenLibro> getImagenes() {
        return imagenes;
    }

    public void setImagenes(List<ImagenLibro> imagenes) {
        this.imagenes = imagenes;
    }
}
