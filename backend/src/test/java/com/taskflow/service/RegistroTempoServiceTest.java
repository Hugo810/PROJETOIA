package com.taskflow.service;

import com.taskflow.dto.RegistroTempoDTO;
import com.taskflow.model.*;
import com.taskflow.repository.RegistroTempoRepository;
import com.taskflow.repository.TarefaRepository;
import com.taskflow.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistroTempoServiceTest {

    @Mock private RegistroTempoRepository repository;
    @Mock private TarefaRepository tarefaRepository;
    @Mock private UsuarioRepository usuarioRepository;

    private RegistroTempoService service;
    private Tarefa tarefa;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        service = new RegistroTempoService(repository, tarefaRepository, usuarioRepository);
        tarefa = new Tarefa();
        tarefa.setId(1L);
        tarefa.setTitulo("Tarefa Teste");
        tarefa.setPrazo(LocalDate.now().plusDays(1));
        tarefa.setStatus(TarefaStatus.PENDENTE);

        usuario = new Usuario(1L, "Joao", "joao@test.com", "123", UsuarioRole.EXECUTOR);
    }

    @Test
    void deveIniciarTimer() {
        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefa));
        when(repository.findByTarefaIdAndEmAndamentoTrue(1L)).thenReturn(Optional.empty());
        when(repository.findByUsuarioIdAndEmAndamentoTrue(1L)).thenReturn(Optional.empty());
        when(usuarioRepository.getReferenceById(1L)).thenReturn(usuario);
        when(repository.save(any())).thenAnswer(i -> {
            RegistroTempo r = i.getArgument(0);
            r.setId(1L);
            return r;
        });

        RegistroTempoDTO resultado = service.iniciarTimer(1L, 1L);

        assertTrue(resultado.getEmAndamento());
        assertFalse(resultado.getManual());
    }

    @Test
    void deveRejeitarTimerDuplicadoNaMesmaTarefa() {
        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefa));
        when(repository.findByTarefaIdAndEmAndamentoTrue(1L)).thenReturn(Optional.of(new RegistroTempo()));

        assertThrows(RuntimeException.class, () -> service.iniciarTimer(1L, 1L));
    }

    @Test
    void deveRejeitarTimerDuplicadoParaOMesmoUsuario() {
        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefa));
        when(repository.findByTarefaIdAndEmAndamentoTrue(1L)).thenReturn(Optional.empty());
        when(repository.findByUsuarioIdAndEmAndamentoTrue(1L)).thenReturn(Optional.of(new RegistroTempo()));

        assertThrows(RuntimeException.class, () -> service.iniciarTimer(1L, 1L));
    }

    @Test
    void devePararTimer() {
        RegistroTempo registro = new RegistroTempo();
        registro.setId(1L);
        registro.setEmAndamento(true);
        registro.setInicio(LocalDateTime.now().minusMinutes(30));
        registro.setUsuario(usuario);
        registro.setTarefa(tarefa);

        when(repository.findById(1L)).thenReturn(Optional.of(registro));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        RegistroTempoDTO resultado = service.pararTimer(1L, 1L);

        assertFalse(resultado.getEmAndamento());
        assertNotNull(resultado.getFim());
        assertNotNull(resultado.getDuracaoMinutos());
    }

    @Test
    void deveRegistrarManual() {
        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefa));
        when(usuarioRepository.getReferenceById(1L)).thenReturn(usuario);
        when(repository.save(any())).thenAnswer(i -> {
            RegistroTempo r = i.getArgument(0);
            r.setId(1L);
            return r;
        });

        RegistroTempoDTO resultado = service.registrarManual(1L, 1L, 60L, "Reunião");

        assertEquals(60L, resultado.getDuracaoMinutos());
        assertTrue(resultado.getManual());
    }

    @Test
    void deveRejeitarRegistroManualComDuracaoInvalida() {
        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefa));

        assertThrows(RuntimeException.class, () -> service.registrarManual(1L, 1L, 0L, "Teste"));
    }

    @Test
    void deveCalcularTotalMinutos() {
        when(repository.sumDuracaoByTarefaId(1L)).thenReturn(120L);

        long total = service.totalMinutosTarefa(1L);

        assertEquals(120L, total);
    }

    @Test
    void deveExcluirRegistro() {
        RegistroTempo registro = new RegistroTempo();
        registro.setId(1L);
        registro.setEmAndamento(false);
        registro.setUsuario(usuario);

        when(repository.findById(1L)).thenReturn(Optional.of(registro));

        service.excluir(1L, 1L);

        verify(repository).deleteById(1L);
    }
}
