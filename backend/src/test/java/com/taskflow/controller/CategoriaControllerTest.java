package com.taskflow.controller;

import com.taskflow.config.CategoriaNotFoundException;
import com.taskflow.dto.CategoriaDTO;
import com.taskflow.service.CategoriaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoriaController.class)
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoriaService service;

    @Test
    void deveListarCategorias() throws Exception {
        when(service.listarTodas()).thenReturn(List.of(
                new CategoriaDTO(1L, "TRABALHO"),
                new CategoriaDTO(2L, "ESTUDOS")
        ));

        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nome").value("TRABALHO"));
    }

    @Test
    void deveBuscarCategoriaPorId() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(new CategoriaDTO(1L, "URGENTE"));

        mockMvc.perform(get("/api/categorias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("URGENTE"));
    }

    @Test
    void deveRetornar404QuandoCategoriaNaoEncontrada() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(new CategoriaNotFoundException(99L));

        mockMvc.perform(get("/api/categorias/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Categoria não encontrada: 99"));
    }

    @Test
    void deveCriarCategoria() throws Exception {
        when(service.criar(any())).thenReturn(new CategoriaDTO(1L, "NOVA"));

        String json = objectMapper.writeValueAsString(new CategoriaDTO(null, "NOVA"));

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("NOVA"));
    }

    @Test
    void deveRejeitarCategoriaComNomeVazio() throws Exception {
        String json = objectMapper.writeValueAsString(new CategoriaDTO(null, ""));

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveAtualizarCategoria() throws Exception {
        when(service.atualizar(eq(1L), any())).thenReturn(new CategoriaDTO(1L, "PROFISSIONAL"));

        String json = objectMapper.writeValueAsString(new CategoriaDTO(null, "PROFISSIONAL"));

        mockMvc.perform(put("/api/categorias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("PROFISSIONAL"));
    }

    @Test
    void deveExcluirCategoria() throws Exception {
        doNothing().when(service).excluir(1L);

        mockMvc.perform(delete("/api/categorias/1"))
                .andExpect(status().isNoContent());
    }
}
