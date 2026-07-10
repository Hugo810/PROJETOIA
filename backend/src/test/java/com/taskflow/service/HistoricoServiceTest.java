package com.taskflow.service;

import com.taskflow.dto.HistoricoDTO;
import com.taskflow.model.*;
import com.taskflow.repository.HistoricoAlteracaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistoricoServiceTest {

    @Mock private HistoricoAlteracaoRepository historicoRepository;

    private HistoricoService service;

    @BeforeEach
    void setUp() {
        service = new HistoricoService(historicoRepository);
    }

    @Test
    void deveListarHistorico() {
        when(historicoRepository.findByTarefaIdOrderByDataAlteracaoDesc(1L)).thenReturn(List.of());

        List<HistoricoDTO> resultado = service.listarPorTarefa(1L);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveRegistrarHistorico() {
        Tarefa tarefa = new Tarefa();
        tarefa.setId(1L);
        Usuario usuario = new Usuario(1L, "Joao", "joao@test.com", "123", UsuarioRole.EXECUTOR);

        service.registrar("titulo", "Antigo", "Novo", tarefa, usuario);

        verify(historicoRepository).save(any());
    }
}
