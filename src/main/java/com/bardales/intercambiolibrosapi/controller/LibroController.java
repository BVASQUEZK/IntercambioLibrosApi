package com.bardales.intercambiolibrosapi.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bardales.intercambiolibrosapi.dto.LibroActualizarDTO;
import com.bardales.intercambiolibrosapi.dto.LibroCreadoDTO;
import com.bardales.intercambiolibrosapi.dto.LibroDTO;
import com.bardales.intercambiolibrosapi.dto.LibroHomeDTO;
import com.bardales.intercambiolibrosapi.dto.LibroRegistroDTO;
import com.bardales.intercambiolibrosapi.service.LibroService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/libros")
@CrossOrigin(origins = "*")
public class LibroController {

    private final LibroService libroService;

    public LibroController(LibroService libroService) {
        this.libroService = libroService;
    }

    @GetMapping("/recientes")
    public List<LibroHomeDTO> listarRecientes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return libroService.obtenerLibrosRecientes(page, size);
    }

    @GetMapping("/buscar")
    public List<LibroDTO> buscarLibros(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Integer idCategoria,
            @RequestParam(required = false) String condicion,
            @RequestParam(required = false) Integer idUsuario,
            @RequestParam(defaultValue = "internacional") String alcance,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return libroService.buscarLibros(query, idCategoria, condicion, idUsuario, alcance, page, size);
    }

    @PostMapping("/registrar")
    public ResponseEntity<LibroCreadoDTO> registrarLibro(
            @RequestHeader("X-User-Id") int idUsuario,
            @Valid @RequestBody LibroRegistroDTO dto) {
        LibroCreadoDTO libro = libroService.registrarLibro(idUsuario, dto);
        return ResponseEntity.status(201).body(libro);
    }

    @PutMapping("/{idLibro}")
    public ResponseEntity<LibroCreadoDTO> actualizarLibro(
            @PathVariable int idLibro,
            @RequestHeader("X-User-Id") int idUsuario,
            @RequestBody LibroActualizarDTO dto) {
        LibroCreadoDTO libro = libroService.actualizarLibro(idUsuario, idLibro, dto);
        return ResponseEntity.ok(libro);
    }

    @DeleteMapping("/{idLibro}")
    public ResponseEntity<Map<String, Object>> eliminarLibroLogico(
            @PathVariable int idLibro,
            @RequestHeader("X-User-Id") int idUsuario) {
        return ResponseEntity.ok(libroService.eliminarLibroLogico(idUsuario, idLibro));
    }
}
