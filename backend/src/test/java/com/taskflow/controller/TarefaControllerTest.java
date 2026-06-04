package com.taskflow.controller;

import com.taskflow.dto.TarefaDTO;
import com.taskflow.service.TarefaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TarefaController.class)
class TarefaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TarefaService service;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private TarefaDTO criarTarefaDTO(Long id, String titulo, String status) {
        TarefaDTO dto = new TarefaDTO();
        dto.setId(id);
        dto.setTitulo(titulo);
        dto.setDescricao("Descrição");
        dto.setCategoriaId(1L);
        dto.setCategoriaNome("TRABALHO");
        dto.setPrazo(LocalDate.now().plusDays(1));
        dto.setStatus(status);
        return dto;
    }

    @Test
    void deveListarTarefas() throws Exception {
        Page<TarefaDTO> page = new PageImpl<>(List.of(
                criarTarefaDTO(1L, "Tarefa 1", "PENDENTE")
        ));
        when(service.listar(any(), any(), any(), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/api/tarefas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].titulo").value("Tarefa 1"));
    }

    @Test
    void deveFiltrarTarefasPorStatus() throws Exception {
        Page<TarefaDTO> page = new PageImpl<>(List.of(
                criarTarefaDTO(1L, "Tarefa Pendente", "PENDENTE")
        ));
        when(service.listar(eq("PENDENTE"), any(), any(), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/api/tarefas?status=PENDENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("PENDENTE"));
    }

    @Test
    void deveFiltrarTarefasPorCategoria() throws Exception {
        Page<TarefaDTO> page = new PageImpl<>(List.of(
                criarTarefaDTO(1L, "Tarefa Trabalho", "PENDENTE")
        ));
        when(service.listar(any(), eq(1L), any(), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/api/tarefas?categoriaId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].categoriaNome").value("TRABALHO"));
    }

    @Test
    void deveBuscarTarefaPorId() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(criarTarefaDTO(1L, "Tarefa", "PENDENTE"));

        mockMvc.perform(get("/api/tarefas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Tarefa"));
    }

    @Test
    void deveCriarTarefa() throws Exception {
        when(service.criar(any(), anyLong())).thenReturn(criarTarefaDTO(1L, "Nova Tarefa", "PENDENTE"));

        TarefaDTO dto = new TarefaDTO();
        dto.setTitulo("Nova Tarefa");
        dto.setDescricao("Desc");
        dto.setCategoriaId(1L);
        dto.setPrazo(LocalDate.now().plusDays(5));

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("X-User-Id", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Nova Tarefa"));
    }

    @Test
    void deveRejeitarTarefaComTituloVazio() throws Exception {
        TarefaDTO dto = new TarefaDTO();
        dto.setTitulo("");
        dto.setCategoriaId(1L);
        dto.setPrazo(LocalDate.now().plusDays(1));

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRejeitarTarefaComPrazoNoPassado() throws Exception {
        TarefaDTO dto = new TarefaDTO();
        dto.setTitulo("Tarefa");
        dto.setCategoriaId(1L);
        dto.setPrazo(LocalDate.now().minusDays(1));

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveAtualizarTarefa() throws Exception {
        when(service.atualizar(eq(1L), any(), anyLong())).thenReturn(
                criarTarefaDTO(1L, "Tarefa Atualizada", "PENDENTE"));

        TarefaDTO dto = new TarefaDTO();
        dto.setTitulo("Tarefa Atualizada");
        dto.setCategoriaId(1L);
        dto.setPrazo(LocalDate.now().plusDays(3));

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put("/api/tarefas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Tarefa Atualizada"));
    }

    @Test
    void deveConcluirTarefa() throws Exception {
        when(service.concluir(eq(1L), anyLong())).thenReturn(
                criarTarefaDTO(1L, "Tarefa", "CONCLUIDA"));

        mockMvc.perform(patch("/api/tarefas/1/concluir")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDA"));
    }

    @Test
    void deveExcluirTarefa() throws Exception {
        doNothing().when(service).excluir(eq(1L), anyLong());

        mockMvc.perform(delete("/api/tarefas/1")
                        .header("X-User-Id", "1"))
                .andExpect(status().isNoContent());
    }
}
