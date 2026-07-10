package com.taskflow.controller;

import com.taskflow.dto.RegraAutomacaoDTO;
import com.taskflow.service.AutomacaoService;
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

@WebMvcTest(AutomacaoController.class)
class AutomacaoControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private AutomacaoService service;

    private RegraAutomacaoDTO criarDTO(Long id) {
        RegraAutomacaoDTO dto = new RegraAutomacaoDTO();
        dto.setId(id);
        dto.setNome("Regra Teste");
        dto.setCondicao("{\"tipo\":\"STATUS_MUDOU\",\"valor\":\"CONCLUIDA\"}");
        dto.setAcao("{\"tipo\":\"ENVIAR_NOTIFICACAO\",\"dados\":{\"usuarioId\":1,\"mensagem\":\"Ok\"}}");
        dto.setAtiva(true);
        dto.setCriadorId(1L);
        dto.setCriadorNome("Joao");
        return dto;
    }

    @Test
    void deveListarRegras() throws Exception {
        when(service.listarTodas()).thenReturn(List.of(criarDTO(1L)));

        mockMvc.perform(get("/api/automacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Regra Teste"));
    }

    @Test
    void deveCriarRegra() throws Exception {
        when(service.criar(any(), eq(1L))).thenReturn(criarDTO(1L));

        mockMvc.perform(post("/api/automacoes")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Regra Teste\",\"condicao\":\"{}\",\"acao\":\"{}\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Regra Teste"));
    }

    @Test
    void deveExcluirRegra() throws Exception {
        doNothing().when(service).excluir(1L);

        mockMvc.perform(delete("/api/automacoes/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveToggleAtiva() throws Exception {
        doNothing().when(service).toggleAtiva(1L, false);

        mockMvc.perform(patch("/api/automacoes/1/ativa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("false"))
                .andExpect(status().isOk());
    }
}
