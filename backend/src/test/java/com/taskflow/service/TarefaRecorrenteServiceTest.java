package com.taskflow.service;

import com.taskflow.dto.TarefaRecorrenteDTO;
import com.taskflow.model.*;
import com.taskflow.repository.CategoriaRepository;
import com.taskflow.repository.TarefaRecorrenteRepository;
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
class TarefaRecorrenteServiceTest {

    @Mock private TarefaRecorrenteRepository repository;
    @Mock private TarefaRepository tarefaRepository;
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private UsuarioRepository usuarioRepository;

    private TarefaRecorrenteService service;

    private Tarefa tarefaModelo;
    private Usuario criador;

    @BeforeEach
    void setUp() {
        service = new TarefaRecorrenteService(repository, tarefaRepository, categoriaRepository, usuarioRepository);

        Categoria cat = new Categoria(1L, "Trabalho");
        criador = new Usuario(1L, "Joao", "joao@test.com", "123", UsuarioRole.EXECUTOR);

        tarefaModelo = new Tarefa();
        tarefaModelo.setId(1L);
        tarefaModelo.setTitulo("Relatório Semanal");
        tarefaModelo.setDescricao("Gerar relatório");
        tarefaModelo.setCategoria(cat);
        tarefaModelo.setPrazo(LocalDate.now().plusDays(1));
        tarefaModelo.setPrioridade(Prioridade.MEDIA);
        tarefaModelo.setStatus(TarefaStatus.PENDENTE);
    }

    @Test
    void deveCriarTarefaRecorrente() {
        TarefaRecorrenteDTO dto = new TarefaRecorrenteDTO();
        dto.setTarefaModeloId(1L);
        dto.setRecorrencia(Recorrencia.SEMANAL);
        dto.setProximaExecucao(LocalDate.now().plusWeeks(1));

        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefaModelo));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(criador));
        when(repository.save(any())).thenAnswer(i -> {
            TarefaRecorrente r = i.getArgument(0);
            r.setId(1L);
            return r;
        });

        TarefaRecorrenteDTO resultado = service.criar(dto, 1L);

        assertEquals(Recorrencia.SEMANAL, resultado.getRecorrencia());
        assertEquals("Relatório Semanal", resultado.getTarefaModeloTitulo());
        assertTrue(resultado.getAtiva());
    }

    @Test
    void deveRejeitarTarefaModeloInexistente() {
        TarefaRecorrenteDTO dto = new TarefaRecorrenteDTO();
        dto.setTarefaModeloId(99L);
        dto.setRecorrencia(Recorrencia.DIARIA);
        dto.setProximaExecucao(LocalDate.now());

        when(tarefaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.criar(dto, 1L));
    }

    @Test
    void devePularExecucao() {
        TarefaRecorrente recorrente = criarRecorrente();
        recorrente.setProximaExecucao(LocalDate.now().plusWeeks(1));

        when(repository.findById(1L)).thenReturn(Optional.of(recorrente));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        TarefaRecorrenteDTO resultado = service.pular(1L);

        assertEquals(LocalDate.now().plusWeeks(2), resultado.getProximaExecucao());
    }

    @Test
    void deveAdiarExecucao() {
        TarefaRecorrente recorrente = criarRecorrente();
        recorrente.setProximaExecucao(LocalDate.now());

        when(repository.findById(1L)).thenReturn(Optional.of(recorrente));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        TarefaRecorrenteDTO resultado = service.adiar(1L, 3);

        assertEquals(LocalDate.now().plusDays(3), resultado.getProximaExecucao());
    }

    @Test
    void deveCalcularProximasExecucoes() {
        TarefaRecorrente recorrente = criarRecorrente();
        recorrente.setProximaExecucao(LocalDate.now());

        when(repository.findById(1L)).thenReturn(Optional.of(recorrente));

        List<LocalDate> datas = service.calcularProximasExecucoes(1L, 4);

        assertEquals(4, datas.size());
        assertEquals(LocalDate.now(), datas.get(0));
        assertEquals(LocalDate.now().plusWeeks(1), datas.get(1));
        assertEquals(LocalDate.now().plusWeeks(2), datas.get(2));
        assertEquals(LocalDate.now().plusWeeks(3), datas.get(3));
    }

    @Test
    void deveExecutarPendentes() {
        TarefaRecorrente recorrente = criarRecorrente();
        recorrente.setProximaExecucao(LocalDate.now());

        when(repository.findByProximaExecucaoLessThanEqualAndAtivaTrue(LocalDate.now())).thenReturn(List.of(recorrente));
        when(tarefaRepository.save(any())).thenAnswer(i -> {
            Tarefa t = i.getArgument(0);
            t.setId(5L);
            return t;
        });
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        List<Tarefa> criadas = service.executarPendentes();

        assertEquals(1, criadas.size());
        assertEquals("Relatório Semanal", criadas.get(0).getTitulo());
        assertEquals(TarefaStatus.PENDENTE, criadas.get(0).getStatus());
    }

    @Test
    void deveDesativarTarefaRecorrente() {
        TarefaRecorrente recorrente = criarRecorrente();
        when(repository.findById(1L)).thenReturn(Optional.of(recorrente));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        TarefaRecorrenteDTO dto = new TarefaRecorrenteDTO();
        dto.setAtiva(false);

        TarefaRecorrenteDTO resultado = service.atualizar(1L, dto);

        assertFalse(resultado.getAtiva());
    }

    @Test
    void deveExcluir() {
        TarefaRecorrente recorrente = criarRecorrente();
        when(repository.findById(1L)).thenReturn(Optional.of(recorrente));

        service.excluir(1L);

        verify(repository).delete(recorrente);
    }

    private TarefaRecorrente criarRecorrente() {
        TarefaRecorrente r = new TarefaRecorrente();
        r.setId(1L);
        r.setTarefaModelo(tarefaModelo);
        r.setRecorrencia(Recorrencia.SEMANAL);
        r.setAtiva(true);
        r.setCriador(criador);
        return r;
    }
}
