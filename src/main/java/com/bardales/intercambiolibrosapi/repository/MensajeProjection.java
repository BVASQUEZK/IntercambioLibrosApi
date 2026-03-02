package com.bardales.intercambiolibrosapi.repository;

import java.time.LocalDateTime;

public interface MensajeProjection {
    Integer getId_mensaje();
    Integer getId_emisor();
    String getContenido();
    LocalDateTime getFecha_envio();
    Boolean getLeido();
}
