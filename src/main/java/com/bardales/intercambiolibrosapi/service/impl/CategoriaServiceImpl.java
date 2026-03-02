package com.bardales.intercambiolibrosapi.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.bardales.intercambiolibrosapi.entity.Categoria;
import com.bardales.intercambiolibrosapi.entity.Libro;
import com.bardales.intercambiolibrosapi.repository.CategoriaRepository;
import com.bardales.intercambiolibrosapi.service.CategoriaService;

@Service
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public List<Categoria> listarCategorias() {
        return categoriaRepository.listarCategorias()
                .stream()
                .map(this::filtrarLibrosActivos)
                .collect(Collectors.toList());
    }

    private Categoria filtrarLibrosActivos(Categoria categoria) {
        List<Libro> activos = categoria.getLibros() == null
                ? List.of()
                : categoria.getLibros()
                        .stream()
                        .filter(this::esActivo)
                        .collect(Collectors.toCollection(ArrayList::new));

        return new Categoria(
                categoria.getIdCategoria(),
                categoria.getNombre(),
                activos);
    }

    private boolean esActivo(Libro libro) {
        String estado = libro.getEstado();
        if (estado == null || estado.isBlank()) {
            return true;
        }
        return "activo".equals(estado.trim().toLowerCase(Locale.ROOT));
    }
}
