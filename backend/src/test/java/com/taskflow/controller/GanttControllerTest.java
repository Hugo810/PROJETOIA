package com.taskflow.controller;

import com.taskflow.dto.*;
import com.taskflow.service.GanttService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GanttController.class)
class GanttControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private GanttService service;

    @Test
    void deveRetornarGantt() throws Exception {
        when(service.buscarTarefas(any(), any())).thenReturn(List.of());
        when(service.listarDependencias(any())).thenReturn(List.of());
        when(service.listarMarcos()).thenReturn(List.of());

        mockMvc.perform(get("/api/gantt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tarefas").isArray())
                .andExpect(jsonPath("$.dependencias").isArray())
                .andExpect(jsonPath("$.marcos").isArray());
    }

    @Test
    void deveListarDependencias() throws Exception {
        when(service.listarDependencias(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/tarefas/1/dependencias"))
                .andExpect(status().isOk());
    }

    @Test
    void deveCriarDependencia() throws Exception {
        DependenciaTarefaDTO dto = new DependenciaTarefaDTO();
        dto.setId(1L);
        dto.setTipo("BLOQUEIA");
        when(service.criarDependencia(eq(1L), eq(2L), any())).thenReturn(dto);

        mockMvc.perform(post("/api/tarefas/1/dependencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tarefaDependenteId\":2,\"tipo\":\"BLOQUEIA\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("BLOQUEIA"));
    }

    @Test
    void deveExcluirDependencia() throws Exception {
        doNothing().when(service).excluirDependencia(1L);

        mockMvc.perform(delete("/api/dependencias/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveListarMarcos() throws Exception {
        when(service.listarMarcos()).thenReturn(List.of());

        mockMvc.perform(get("/api/marcos"))
                .andExpect(status().isOk());
    }

    @Test
    void deveCriarMarco() throws Exception {
        MarcoDTO dto = new MarcoDTO();
        dto.setId(1L);
        dto.setNome("Marco");
        when(service.criarMarco(any(), eq(1L))).thenReturn(dto);

        mockMvc.perform(post("/api/marcos")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Marco\",\"data\":\"2026-08-01T10:00:00\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Marco"));
    }
}
