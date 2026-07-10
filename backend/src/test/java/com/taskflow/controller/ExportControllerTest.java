package com.taskflow.controller;

import com.taskflow.dto.TarefaDTO;
import com.taskflow.model.TarefaStatus;
import com.taskflow.service.ExportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExportController.class)
class ExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExportService exportService;

    @Test
    void exportarJSON() throws Exception {
        TarefaDTO dto = new TarefaDTO();
        dto.setId(1L);
        dto.setTitulo("Tarefa teste");
        dto.setStatus("PENDENTE");
        dto.setPrazo(LocalDate.of(2026, 7, 15));
        dto.setCategoriaNome("Backend");

        when(exportService.exportarJSON()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/exportar/tarefas").param("formato", "json"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void exportarCSV() throws Exception {
        when(exportService.exportarCSV()).thenReturn("ID,Título\n1,Tarefa teste\n");

        mockMvc.perform(get("/api/exportar/tarefas").param("formato", "csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=tarefas.csv"));
    }
}
