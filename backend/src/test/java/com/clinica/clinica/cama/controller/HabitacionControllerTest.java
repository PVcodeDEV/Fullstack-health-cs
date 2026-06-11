package com.clinica.clinica.cama.controller;

import com.clinica.clinica.cama.dto.HabitacionResponse;
import com.clinica.clinica.cama.service.HabitacionService;
import com.clinica.config.GlobalExceptionHandler;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HabitacionController.class)
@Import(GlobalExceptionHandler.class)
class HabitacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HabitacionService service;

    @Test
    void findAll_ShouldReturn200() throws Exception {
        when(service.findAll()).thenReturn(List.of(
                new HabitacionResponse(1L, 1L, "Privada", "Habitación 101", "Piso 1", 2, true)));

        mockMvc.perform(get("/api/v1/habitaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Habitación 101"));
    }

    @Test
    void findById_ShouldReturn200() throws Exception {
        when(service.findById(1L)).thenReturn(
                new HabitacionResponse(1L, 1L, "Privada", "Habitación 101", "Piso 1", 2, true));

        mockMvc.perform(get("/api/v1/habitaciones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Habitación 101"));
    }

    @Test
    void findById_ShouldReturn404() throws Exception {
        when(service.findById(99L)).thenThrow(new EntityNotFoundException("Habitacion no encontrada con id: 99"));

        mockMvc.perform(get("/api/v1/habitaciones/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_ShouldReturn201() throws Exception {
        when(service.create(any())).thenReturn(
                new HabitacionResponse(1L, 1L, "Privada", "Nueva Habitación", "Piso 2", 4, true));

        mockMvc.perform(post("/api/v1/habitaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoHabitacionId\":1,\"nombre\":\"Nueva Habitación\",\"ubicacion\":\"Piso 2\",\"capacidad\":4}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Nueva Habitación"));
    }

    @Test
    void create_WithInvalidBody_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/habitaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoHabitacionId\":null,\"nombre\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_ShouldReturn200() throws Exception {
        when(service.update(any(), any())).thenReturn(
                new HabitacionResponse(1L, 1L, "VIP", "Actualizada", "Piso 3", 1, true));

        mockMvc.perform(put("/api/v1/habitaciones/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoHabitacionId\":1,\"nombre\":\"Actualizada\",\"ubicacion\":\"Piso 3\",\"capacidad\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Actualizada"));
    }

    @Test
    void softDelete_ShouldReturn200() throws Exception {
        when(service.softDelete(1L)).thenReturn(
                new HabitacionResponse(1L, 1L, "Privada", "Habitación 101", "Piso 1", 2, false));

        mockMvc.perform(delete("/api/v1/habitaciones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
    }
}
