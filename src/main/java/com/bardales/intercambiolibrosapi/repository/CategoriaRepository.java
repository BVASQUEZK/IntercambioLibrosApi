package com.bardales.intercambiolibrosapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bardales.intercambiolibrosapi.entity.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    @Query(value = "SELECT c.id_categoria, c.nombre FROM categoria c ORDER BY c.nombre ASC", nativeQuery = true)
    List<Categoria> listarCategorias();
}
