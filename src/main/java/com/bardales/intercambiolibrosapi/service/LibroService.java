package com.bardales.intercambiolibrosapi.service;

import java.util.List;
import java.util.Map;

import com.bardales.intercambiolibrosapi.dto.LibroActualizarDTO;
import com.bardales.intercambiolibrosapi.dto.LibroCreadoDTO;
import com.bardales.intercambiolibrosapi.dto.LibroDTO;
import com.bardales.intercambiolibrosapi.dto.LibroHomeDTO;
import com.bardales.intercambiolibrosapi.dto.LibroRegistroDTO;

public interface LibroService {
    List<LibroHomeDTO> obtenerLibrosRecientes(int pagina, int cantidad);
    List<LibroDTO> buscarLibros(
            String query,
            Integer idCategoria,
            String estado,
            Integer idUsuario,
            String alcance,
            int pagina,
            int cantidad);
    LibroCreadoDTO registrarLibro(int idUsuario, LibroRegistroDTO dto);
    LibroCreadoDTO actualizarLibro(int idUsuario, int idLibro, LibroActualizarDTO dto);
    Map<String, Object> eliminarLibroLogico(int idUsuario, int idLibro);
}
