package com.taskflow.controller;

import com.taskflow.model.Tarefa;
import com.taskflow.model.TarefaStatus;
import com.taskflow.repository.TarefaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CalendarioController.class)
class CalendarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TarefaRepository tarefaRepository;

    @Test
    void exportarICS() throws Exception {
        Tarefa t = new Tarefa();
        t.setId(1L);
        t.setTitulo("Reuniao");
        t.setDescricao("Daily standup");
        t.setPrazo(LocalDate.of(2026, 7, 10));
        t.setStatus(TarefaStatus.PENDENTE);

        when(tarefaRepository.findAll()).thenReturn(List.of(t));

        mockMvc.perform(get("/api/calendario/tarefas/ics"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/calendar"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=tarefas.ics"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("BEGIN:VCALENDAR")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("SUMMARY:Reuniao")));
    }

    @Test
    void exportarICSVazio() throws Exception {
        when(tarefaRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/calendario/tarefas/ics"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("END:VCALENDAR")));
    }

    @Test
    void exportarICSComUsuario() throws Exception {
        Tarefa t = new Tarefa();
        t.setId(2L);
        t.setTitulo("Tarefa usuario");
        t.setPrazo(LocalDate.of(2026, 7, 20));
        t.setStatus(TarefaStatus.EM_EXECUCAO);

        when(tarefaRepository.findByResponsavelIdOrderByPrazoAsc(any(), any()))
                .thenReturn(new PageImpl<>(List.of(t), PageRequest.of(0, 1000), 1));

        mockMvc.perform(get("/api/calendario/tarefas/ics").param("usuarioId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Tarefa usuario")));
    }
}
