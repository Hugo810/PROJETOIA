package com.taskflow.controller;

import com.taskflow.dto.MetaDTO;
import com.taskflow.dto.ProjetoDTO;
import com.taskflow.service.ProjetoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjetoController.class)
class ProjetoControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private ProjetoService service;

    private ProjetoDTO criarDTO(Long id) {
        ProjetoDTO dto = new ProjetoDTO();
        dto.setId(id);
        dto.setNome("Projeto Teste");
        dto.setStatus("PLANEJADO");
        dto.setTotalMetas(0L);
        dto.setMetasConcluidas(0L);
        dto.setProgressoGeral(0.0);
        return dto;
    }

    @Test
    void deveListarProjetos() throws Exception {
        when(service.listarTodos()).thenReturn(List.of(criarDTO(1L)));

        mockMvc.perform(get("/api/projetos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Projeto Teste"));
    }

    @Test
    void deveBuscarProjeto() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(criarDTO(1L));

        mockMvc.perform(get("/api/projetos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Projeto Teste"));
    }

    @Test
    void deveCriarProjeto() throws Exception {
        when(service.criar(any(), eq(1L))).thenReturn(criarDTO(1L));

        mockMvc.perform(post("/api/projetos")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Projeto Teste\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Projeto Teste"));
    }

    @Test
    void deveExcluirProjeto() throws Exception {
        doNothing().when(service).excluir(1L);

        mockMvc.perform(delete("/api/projetos/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveListarMetas() throws Exception {
        when(service.listarMetas(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/projetos/1/metas"))
                .andExpect(status().isOk());
    }

    @Test
    void deveCriarMeta() throws Exception {
        MetaDTO meta = new MetaDTO();
        meta.setId(1L);
        meta.setTitulo("Meta 1");
        when(service.criarMeta(eq(1L), any())).thenReturn(meta);

        mockMvc.perform(post("/api/projetos/1/metas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Meta 1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Meta 1"));
    }
}
