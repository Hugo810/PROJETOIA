package com.taskflow.controller;

import com.taskflow.dto.RegistroTempoDTO;
import com.taskflow.service.RegistroTempoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RegistroTempoController.class)
class RegistroTempoControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private RegistroTempoService service;

    private RegistroTempoDTO criarRegistro(Long id, Long duracao) {
        RegistroTempoDTO dto = new RegistroTempoDTO();
        dto.setId(id);
        dto.setTarefaId(1L);
        dto.setTarefaTitulo("Tarefa");
        dto.setUsuarioId(1L);
        dto.setUsuarioNome("Joao");
        dto.setInicio(LocalDateTime.now().minusHours(1));
        dto.setDuracaoMinutos(duracao);
        dto.setManual(false);
        dto.setEmAndamento(false);
        return dto;
    }

    @Test
    void deveListarRegistros() throws Exception {
        when(service.listarPorTarefa(1L)).thenReturn(List.of(criarRegistro(1L, 30L)));
        when(service.totalMinutosTarefa(1L)).thenReturn(30L);

        mockMvc.perform(get("/api/tarefas/1/tempo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMinutos").value(30));
    }

    @Test
    void deveIniciarTimer() throws Exception {
        RegistroTempoDTO dto = criarRegistro(1L, null);
        dto.setEmAndamento(true);
        when(service.iniciarTimer(1L, 1L)).thenReturn(dto);

        mockMvc.perform(post("/api/tarefas/1/tempo")
                        .header("X-User-Id", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.emAndamento").value(true));
    }

    @Test
    void devePararTimer() throws Exception {
        when(service.pararTimer(1L, 1L)).thenReturn(criarRegistro(1L, 30L));

        mockMvc.perform(patch("/api/registros-tempo/1/parar")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duracaoMinutos").value(30));
    }

    @Test
    void deveRegistrarManual() throws Exception {
        when(service.registrarManual(eq(1L), eq(1L), eq(45L), any())).thenReturn(criarRegistro(1L, 45L));

        mockMvc.perform(post("/api/tarefas/1/tempo/manual")
                        .header("X-User-Id", "1")
                        .contentType("application/json")
                        .content("{\"duracaoMinutos\":45,\"descricao\":\"Teste\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.duracaoMinutos").value(45));
    }
}
