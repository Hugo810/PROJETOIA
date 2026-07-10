package com.taskflow.controller;

import com.taskflow.service.ImportService;
import com.taskflow.model.Tarefa;
import com.taskflow.model.TarefaStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ImportController.class)
class ImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImportService importService;

    @Test
    void importarCSV() throws Exception {
        Tarefa t = new Tarefa();
        t.setId(1L);
        t.setTitulo("Importada");
        t.setStatus(TarefaStatus.PENDENTE);

        when(importService.importarCSV(any())).thenReturn(List.of(t));

        MockMultipartFile file = new MockMultipartFile(
                "file", "tarefas.csv", "text/csv",
                "ID,Título\n1,Importada\n".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/importar/tarefas").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("1 tarefas importadas com sucesso"));
    }

    @Test
    void importarJSON() throws Exception {
        Tarefa t = new Tarefa();
        t.setId(1L);
        t.setTitulo("Importada JSON");
        t.setStatus(TarefaStatus.PENDENTE);

        when(importService.importarJSON(any())).thenReturn(List.of(t));

        MockMultipartFile file = new MockMultipartFile(
                "file", "tarefas.json", "application/json",
                "[{\"titulo\":\"Importada JSON\"}]".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/importar/tarefas").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1));
    }
}
