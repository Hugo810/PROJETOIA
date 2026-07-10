package com.taskflow.controller;

import com.taskflow.dto.NotificacaoDTO;
import com.taskflow.service.NotificacaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificacaoController.class)
class NotificacaoControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private NotificacaoService service;

    private NotificacaoDTO criarNotificacao(Long id, String mensagem) {
        NotificacaoDTO dto = new NotificacaoDTO();
        dto.setId(id);
        dto.setMensagem(mensagem);
        dto.setUsuarioId(1L);
        dto.setLida(false);
        dto.setDataCriacao(LocalDateTime.now());
        dto.setTipo("COMENTARIO");
        return dto;
    }

    @Test
    void deveListarNotificacoes() throws Exception {
        when(service.listarPorUsuario(1L)).thenReturn(List.of(criarNotificacao(1L, "Teste")));

        mockMvc.perform(get("/api/notificacoes?usuarioId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mensagem").value("Teste"));
    }

    @Test
    void deveContarNaoLidas() throws Exception {
        when(service.countNaoLidas(1L)).thenReturn(3L);

        mockMvc.perform(get("/api/notificacoes/contagem?usuarioId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.naoLidas").value(3));
    }

    @Test
    void deveMarcarComoLida() throws Exception {
        doNothing().when(service).marcarComoLida(1L);

        mockMvc.perform(patch("/api/notificacoes/1/lida"))
                .andExpect(status().isOk());
    }

    @Test
    void deveMarcarTodasComoLidas() throws Exception {
        doNothing().when(service).marcarTodasComoLidas(1L);

        mockMvc.perform(patch("/api/notificacoes/lidas?usuarioId=1"))
                .andExpect(status().isOk());
    }
}
