package com.taskflow.service;

import com.taskflow.dto.*;
import com.taskflow.model.*;
import com.taskflow.repository.*;
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
class GanttServiceTest {

    @Mock private TarefaRepository tarefaRepository;
    @Mock private DependenciaTarefaRepository dependenciaRepository;
    @Mock private MarcoRepository marcoRepository;
    @Mock private UsuarioRepository usuarioRepository;

    private GanttService service;

    private Tarefa tarefa1;
    private Tarefa tarefa2;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        service = new GanttService(tarefaRepository, dependenciaRepository, marcoRepository, usuarioRepository);

        Categoria cat = new Categoria(1L, "Trabalho");
        usuario = new Usuario(1L, "Joao", "joao@test.com", "123", UsuarioRole.EXECUTOR);

        tarefa1 = new Tarefa();
        tarefa1.setId(1L);
        tarefa1.setTitulo("Tarefa A");
        tarefa1.setPrazo(LocalDate.now().plusDays(5));
        tarefa1.setStatus(TarefaStatus.PENDENTE);
        tarefa1.setPrioridade(Prioridade.MEDIA);
        tarefa1.setCategoria(cat);
        tarefa1.setResponsavel(usuario);
        tarefa1.setDataCriacao(LocalDateTime.now().minusDays(2));

        tarefa2 = new Tarefa();
        tarefa2.setId(2L);
        tarefa2.setTitulo("Tarefa B");
        tarefa2.setPrazo(LocalDate.now().plusDays(10));
        tarefa2.setStatus(TarefaStatus.EM_EXECUCAO);
        tarefa2.setPrioridade(Prioridade.ALTA);
        tarefa2.setCategoria(cat);
    }

    @Test
    void deveBuscarTarefasGantt() {
        when(tarefaRepository.findByPrazoBetween(any(), any())).thenReturn(List.of(tarefa1, tarefa2));

        List<GanttTarefa> resultado = service.buscarTarefas(null, null);

        assertEquals(2, resultado.size());
        assertEquals("Tarefa A", resultado.get(0).getTitulo());
        assertTrue(resultado.get(0).getDuracaoDias() >= 1);
    }

    @Test
    void deveListarDependencias() {
        DependenciaTarefa dep = new DependenciaTarefa();
        dep.setId(1L);
        dep.setTarefa(tarefa1);
        dep.setTarefaDependente(tarefa2);
        dep.setTipo(TipoDependencia.BLOQUEIA);

        when(dependenciaRepository.findByTarefaId(1L)).thenReturn(List.of(dep));

        List<DependenciaTarefaDTO> resultado = service.listarDependencias(1L);

        assertEquals(1, resultado.size());
        assertEquals("BLOQUEIA", resultado.get(0).getTipo());
    }

    @Test
    void deveCriarDependencia() {
        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefa1));
        when(tarefaRepository.findById(2L)).thenReturn(Optional.of(tarefa2));
        when(dependenciaRepository.save(any())).thenAnswer(i -> {
            DependenciaTarefa d = i.getArgument(0);
            d.setId(1L);
            return d;
        });

        DependenciaTarefaDTO resultado = service.criarDependencia(1L, 2L, TipoDependencia.BLOQUEIA);

        assertEquals("BLOQUEIA", resultado.getTipo());
        assertEquals(1L, resultado.getTarefaId());
        assertEquals(2L, resultado.getTarefaDependenteId());
    }

    @Test
    void deveRejeitarDependenciaPropria() {
        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefa1));

        assertThrows(RuntimeException.class, () -> service.criarDependencia(1L, 1L, TipoDependencia.BLOQUEIA));
    }

    @Test
    void deveExcluirDependencia() {
        service.excluirDependencia(1L);
        verify(dependenciaRepository).deleteById(1L);
    }

    @Test
    void deveCriarMarco() {
        MarcoDTO dto = new MarcoDTO();
        dto.setNome("Entrega Final");
        dto.setData(LocalDateTime.now().plusDays(30));

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(marcoRepository.save(any())).thenAnswer(i -> {
            Marco m = i.getArgument(0);
            m.setId(1L);
            return m;
        });

        MarcoDTO resultado = service.criarMarco(dto, 1L);

        assertEquals("Entrega Final", resultado.getNome());
        assertEquals(1L, resultado.getCriadorId());
    }

    @Test
    void deveListarMarcos() {
        Marco marco = new Marco();
        marco.setId(1L);
        marco.setNome("Marco 1");
        when(marcoRepository.findAllByOrderByDataAsc()).thenReturn(List.of(marco));

        List<MarcoDTO> resultado = service.listarMarcos();

        assertEquals(1, resultado.size());
        assertEquals("Marco 1", resultado.get(0).getNome());
    }
}
