package com.taskflow.service;

import com.taskflow.dto.NotificacaoDTO;
import com.taskflow.model.*;
import com.taskflow.repository.NotificacaoRepository;
import com.taskflow.repository.TarefaRepository;
import com.taskflow.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacaoServiceTest {

    @Mock private NotificacaoRepository notificacaoRepository;
    @Mock private TarefaRepository tarefaRepository;
    @Mock private UsuarioRepository usuarioRepository;

    private NotificacaoService service;

    @BeforeEach
    void setUp() {
        service = new NotificacaoService(notificacaoRepository, tarefaRepository, usuarioRepository);
    }

    @Test
    void deveListarNotificacoes() {
        when(notificacaoRepository.findByUsuarioIdOrderByDataCriacaoDesc(1L)).thenReturn(List.of());

        List<NotificacaoDTO> resultado = service.listarPorUsuario(1L);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveContarNaoLidas() {
        when(notificacaoRepository.countByUsuarioIdAndLidaFalse(1L)).thenReturn(3L);

        long count = service.countNaoLidas(1L);

        assertEquals(3L, count);
    }

    @Test
    void deveCriarNotificacao() {
        Usuario usuario = new Usuario(1L, "Joao", "joao@test.com", "123", UsuarioRole.EXECUTOR);
        when(usuarioRepository.getReferenceById(1L)).thenReturn(usuario);

        service.criar("Teste notificacao", 1L, null, "COMENTARIO");

        verify(notificacaoRepository).save(any());
    }

    @Test
    void naoDeveCriarNotificacaoSemUsuario() {
        service.criar("Teste", null, null, "COMENTARIO");

        verify(notificacaoRepository, never()).save(any());
    }
}
