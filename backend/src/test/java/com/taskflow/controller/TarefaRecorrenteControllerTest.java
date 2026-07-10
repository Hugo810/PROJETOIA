package com.taskflow.controller;

import com.taskflow.dto.TarefaRecorrenteDTO;
import com.taskflow.model.Recorrencia;
import com.taskflow.service.TarefaRecorrenteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TarefaRecorrenteController.class)
class TarefaRecorrenteControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private TarefaRecorrenteService service;

    private TarefaRecorrenteDTO criarDTO(Long id) {
        TarefaRecorrenteDTO dto = new TarefaRecorrenteDTO();
        dto.setId(id);
        dto.setTarefaModeloId(1L);
        dto.setTarefaModeloTitulo("Relatório");
        dto.setRecorrencia(Recorrencia.SEMANAL);
        dto.setProximaExecucao(LocalDate.now().plusWeeks(1));
        dto.setAtiva(true);
        dto.setCriadorId(1L);
        dto.setCriadorNome("Joao");
        return dto;
    }

    @Test
    void deveListarTarefasRecorrentes() throws Exception {
        when(service.listarTodas()).thenReturn(List.of(criarDTO(1L)));

        mockMvc.perform(get("/api/tarefas-recorrentes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].recorrencia").value("SEMANAL"));
    }

    @Test
    void deveCriarTarefaRecorrente() throws Exception {
        when(service.criar(any(), eq(1L))).thenReturn(criarDTO(1L));

        mockMvc.perform(post("/api/tarefas-recorrentes")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tarefaModeloId\":1,\"recorrencia\":\"SEMANAL\",\"proximaExecucao\":\"2026-07-17\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recorrencia").value("SEMANAL"));
    }

    @Test
    void devePularExecucao() throws Exception {
        TarefaRecorrenteDTO dto = criarDTO(1L);
        dto.setProximaExecucao(LocalDate.now().plusWeeks(2));
        when(service.pular(1L)).thenReturn(dto);

        mockMvc.perform(patch("/api/tarefas-recorrentes/1/pular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.proximaExecucao").value(dto.getProximaExecucao().toString()));
    }

    @Test
    void deveExcluirTarefaRecorrente() throws Exception {
        doNothing().when(service).excluir(1L);

        mockMvc.perform(delete("/api/tarefas-recorrentes/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveCalcularProximasExecucoes() throws Exception {
        when(service.calcularProximasExecucoes(1L, 6)).thenReturn(
                List.of(LocalDate.now(), LocalDate.now().plusWeeks(1), LocalDate.now().plusWeeks(2)));

        mockMvc.perform(get("/api/tarefas-recorrentes/1/proximas").param("quantidade", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(LocalDate.now().toString()));
    }
}
