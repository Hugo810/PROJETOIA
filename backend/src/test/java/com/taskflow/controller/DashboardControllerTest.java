package com.taskflow.controller;

import com.taskflow.dto.*;
import com.taskflow.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private DashboardService service;

    @Test
    void deveRetornarDashboard() throws Exception {
        DashboardResumo resumo = new DashboardResumo();
        resumo.setTotalTarefas(10);
        when(service.resumo()).thenReturn(resumo);
        when(service.porStatus()).thenReturn(List.of());
        when(service.porCategoria()).thenReturn(List.of());
        when(service.porResponsavel()).thenReturn(List.of());
        when(service.tendencia()).thenReturn(List.of());

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo.totalTarefas").value(10));
    }

    @Test
    void deveRetornarResumo() throws Exception {
        DashboardResumo resumo = new DashboardResumo();
        resumo.setPendentes(5);
        when(service.resumo()).thenReturn(resumo);

        mockMvc.perform(get("/api/dashboard/resumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendentes").value(5));
    }

    @Test
    void deveRetornarPorStatus() throws Exception {
        when(service.porStatus()).thenReturn(List.of(
                new TarefasPorStatus("PENDENTE", 5),
                new TarefasPorStatus("EM_EXECUCAO", 3)
        ));

        mockMvc.perform(get("/api/dashboard/por-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDENTE"))
                .andExpect(jsonPath("$[0].contagem").value(5));
    }

    @Test
    void deveRetornarPorCategoria() throws Exception {
        when(service.porCategoria()).thenReturn(List.of(
                new TarefasPorCategoria(1L, "Trabalho", 8)
        ));

        mockMvc.perform(get("/api/dashboard/por-categoria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoria").value("Trabalho"));
    }

    @Test
    void deveRetornarPorResponsavel() throws Exception {
        when(service.porResponsavel()).thenReturn(List.of(
                new TarefasPorResponsavel(1L, "Joao", 10, 7)
        ));

        mockMvc.perform(get("/api/dashboard/por-responsavel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].responsavel").value("Joao"));
    }

    @Test
    void deveRetornarTendencia() throws Exception {
        when(service.tendencia()).thenReturn(List.of(
                new Tendencia("10/07", 3, 2)
        ));

        mockMvc.perform(get("/api/dashboard/tendencia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].data").value("10/07"));
    }
}
