package com.taskflow.controller;

import com.taskflow.dto.ComentarioDTO;
import com.taskflow.service.ComentarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ComentarioController.class)
class ComentarioControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private ComentarioService service;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private ComentarioDTO criarComentario(Long id, String texto) {
        ComentarioDTO dto = new ComentarioDTO();
        dto.setId(id);
        dto.setTexto(texto);
        dto.setTarefaId(1L);
        dto.setAutorId(1L);
        dto.setAutorNome("Joao");
        dto.setDataCriacao(LocalDateTime.now());
        return dto;
    }

    @Test
    void deveListarComentarios() throws Exception {
        when(service.listarPorTarefa(1L)).thenReturn(List.of(criarComentario(1L, "Comentario 1")));

        mockMvc.perform(get("/api/tarefas/1/comentarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].texto").value("Comentario 1"));
    }

    @Test
    void deveCriarComentario() throws Exception {
        when(service.criar(any(), eq(1L))).thenReturn(criarComentario(1L, "Novo comentario"));

        ComentarioDTO dto = new ComentarioDTO();
        dto.setTexto("Novo comentario");
        dto.setTarefaId(1L);

        mockMvc.perform(post("/api/tarefas/1/comentarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .header("X-User-Id", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.texto").value("Novo comentario"));
    }

    @Test
    void deveExcluirComentario() throws Exception {
        doNothing().when(service).excluir(1L, 1L);

        mockMvc.perform(delete("/api/comentarios/1")
                        .header("X-User-Id", "1"))
                .andExpect(status().isNoContent());
    }
}
