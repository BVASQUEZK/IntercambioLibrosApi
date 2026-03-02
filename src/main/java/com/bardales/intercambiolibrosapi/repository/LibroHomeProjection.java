package com.bardales.intercambiolibrosapi.repository;

public interface LibroHomeProjection {
    Integer getId_libro();
    Integer getId_usuario();
    String getTitulo();
    String getAutor();
    String getUrl_portada();
    String getDistrito();
    String getDepartamento();
}
