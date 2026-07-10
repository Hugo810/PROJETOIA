package com.taskflow.service;

import com.taskflow.dto.ComentarioDTO;
import com.taskflow.model.*;
import com.taskflow.repository.ComentarioRepository;
import com.taskflow.repository.TarefaRepository;
import com.taskflow.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComentarioServiceTest {

    @Mock private ComentarioRepository comentarioRepository;
    @Mock private TarefaRepository tarefaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private NotificacaoService notificacaoService;

    private ComentarioService service;

    @BeforeEach
    void setUp() {
        service = new ComentarioService(comentarioRepository, tarefaRepository, usuarioRepository, notificacaoService);
    }

    @Test
    void deveCriarComentario() {
        Tarefa tarefa = new Tarefa();
        tarefa.setId(1L);
        tarefa.setTitulo("Tarefa Teste");
        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefa));

        Usuario autor = new Usuario(1L, "Joao", "joao@test.com", "123", UsuarioRole.EXECUTOR);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(autor));

        when(comentarioRepository.save(any())).thenAnswer(i -> {
            Comentario c = i.getArgument(0);
            c.setId(1L);
            return c;
        });

        ComentarioDTO dto = new ComentarioDTO();
        dto.setTexto("Meu comentario");
        dto.setTarefaId(1L);

        ComentarioDTO resultado = service.criar(dto, 1L);

        assertEquals("Meu comentario", resultado.getTexto());
        assertEquals(1L, resultado.getAutorId());
    }

    @Test
    void deveListarComentarios() {
        when(comentarioRepository.findByTarefaIdOrderByDataCriacaoAsc(1L)).thenReturn(List.of());

        List<ComentarioDTO> resultado = service.listarPorTarefa(1L);

        assertTrue(resultado.isEmpty());
    }
}
