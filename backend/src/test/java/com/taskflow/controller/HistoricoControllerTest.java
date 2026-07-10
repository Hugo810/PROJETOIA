package com.taskflow.controller;

import com.taskflow.dto.HistoricoDTO;
import com.taskflow.service.HistoricoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HistoricoController.class)
class HistoricoControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private HistoricoService service;

    @Test
    void deveListarHistorico() throws Exception {
        HistoricoDTO dto = new HistoricoDTO();
        dto.setId(1L);
        dto.setTarefaId(1L);
        dto.setTarefaTitulo("Tarefa");
        dto.setUsuarioNome("Joao");
        dto.setCampo("titulo");
        dto.setValorAnterior("Antigo");
        dto.setValorNovo("Novo");
        dto.setDataAlteracao(LocalDateTime.now());

        when(service.listarPorTarefa(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/tarefas/1/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].campo").value("titulo"));
    }
}
