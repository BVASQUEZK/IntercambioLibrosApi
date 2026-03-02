package com.bardales.intercambiolibrosapi.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bardales.intercambiolibrosapi.entity.Categoria;
import com.bardales.intercambiolibrosapi.service.CategoriaService;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping("/listar")
    public List<Categoria> listarCategorias() {
        return categoriaService.listarCategorias();
    }
}
