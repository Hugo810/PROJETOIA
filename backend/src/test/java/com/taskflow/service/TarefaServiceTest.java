package com.taskflow.service;

import com.taskflow.dto.ResumoTarefasDTO;
import com.taskflow.dto.TarefaDTO;
import com.taskflow.model.Categoria;
import com.taskflow.model.Tarefa;
import com.taskflow.model.TarefaStatus;
import com.taskflow.model.Usuario;
import com.taskflow.model.UsuarioRole;
import com.taskflow.repository.TarefaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TarefaServiceTest {

    @Mock
    private TarefaRepository tarefaRepository;

    @Mock
    private CategoriaService categoriaService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private HistoricoService historicoService;

    @Mock
    private NotificacaoService notificacaoService;

    @Mock
    private AutomacaoService automacaoService;

    private TarefaService service;

    private Categoria categoria;

    @BeforeEach
    void setUp() {
        service = new TarefaService(tarefaRepository, categoriaService, usuarioService, historicoService, notificacaoService, automacaoService);
        categoria = new Categoria(1L, "TRABALHO");
    }

    @Test
    void deveListarTarefasPaginadas() {
        Tarefa tarefa = criarTarefa(1L, "Tarefa 1", LocalDate.now().plusDays(1));
        Page<Tarefa> page = new PageImpl<>(List.of(tarefa));

        when(tarefaRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(PageRequest.class))).thenReturn(page);

        Page<TarefaDTO> resultado = service.listar(null, null, null, null, null, 0, 10);

        assertEquals(1, resultado.getContent().size());
        assertEquals("Tarefa 1", resultado.getContent().get(0).getTitulo());
    }

    @Test
    void deveFiltrarPorStatus() {
        Tarefa tarefa = criarTarefa(1L, "Tarefa Pendente", LocalDate.now().plusDays(1));
        Page<Tarefa> page = new PageImpl<>(List.of(tarefa));

        when(tarefaRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(PageRequest.class)))
                .thenReturn(page);

        Page<TarefaDTO> resultado = service.listar("PENDENTE", null, null, null, null, 0, 10);

        assertEquals(1, resultado.getContent().size());
    }

    @Test
    void deveFiltrarPorCategoria() {
        Tarefa tarefa = criarTarefa(1L, "Tarefa Trabalho", LocalDate.now().plusDays(1));
        Page<Tarefa> page = new PageImpl<>(List.of(tarefa));

        when(tarefaRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(PageRequest.class)))
                .thenReturn(page);

        Page<TarefaDTO> resultado = service.listar(null, 1L, null, null, null, 0, 10);

        assertEquals(1, resultado.getContent().size());
    }

    @Test
    void deveFiltrarPorStatusECategoria() {
        Tarefa tarefa = criarTarefa(1L, "Tarefa", LocalDate.now().plusDays(1));
        Page<Tarefa> page = new PageImpl<>(List.of(tarefa));

        when(tarefaRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(PageRequest.class)))
                .thenReturn(page);

        Page<TarefaDTO> resultado = service.listar("PENDENTE", 1L, null, null, null, 0, 10);

        assertEquals(1, resultado.getContent().size());
    }

    @Test
    void deveBuscarTarefaPorId() {
        Tarefa tarefa = criarTarefa(1L, "Tarefa Teste", LocalDate.now().plusDays(1));
        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefa));

        TarefaDTO dto = service.buscarPorId(1L);

        assertEquals("Tarefa Teste", dto.getTitulo());
    }

    @Test
    void deveLancarExcecaoQuandoTarefaNaoEncontrada() {
        when(tarefaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.buscarPorId(99L));
    }

    @Test
    void deveCriarTarefa() {
        doNothing().when(usuarioService).validarRole(anyLong(), any());
        when(categoriaService.findEntityById(1L)).thenReturn(categoria);
        when(tarefaRepository.save(any())).thenAnswer(i -> {
            Tarefa t = i.getArgument(0);
            t.setId(1L);
            return t;
        });

        TarefaDTO dto = new TarefaDTO();
        dto.setTitulo("Nova Tarefa");
        dto.setDescricao("Descrição");
        dto.setCategoriaId(1L);
        dto.setPrazo(LocalDate.now().plusDays(5));

        TarefaDTO resultado = service.criar(dto, 1L);

        assertEquals("Nova Tarefa", resultado.getTitulo());
        assertEquals("PENDENTE", resultado.getStatus());
    }

    @Test
    void deveAtualizarTarefa() {
        doNothing().when(usuarioService).validarRole(anyLong(), any());

        Usuario usuario = new Usuario(1L, "Admin", "admin@test.com", "123", UsuarioRole.ADMIN);
        when(usuarioService.findEntityById(1L)).thenReturn(usuario);

        Tarefa tarefa = criarTarefa(1L, "Tarefa Antiga", LocalDate.now().plusDays(1));
        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefa));
        when(tarefaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        TarefaDTO dto = new TarefaDTO();
        dto.setTitulo("Tarefa Atualizada");
        dto.setDescricao("Nova descrição");
        dto.setPrazo(LocalDate.now().plusDays(3));
        dto.setCategoriaId(1L);

        TarefaDTO resultado = service.atualizar(1L, dto, 1L);

        assertEquals("Tarefa Atualizada", resultado.getTitulo());
    }

    @Test
    void deveIniciarTarefa() {
        Usuario usuario = new Usuario(1L, "Admin", "admin@test.com", "123", UsuarioRole.ADMIN);
        when(usuarioService.findEntityById(1L)).thenReturn(usuario);

        Tarefa tarefa = criarTarefa(1L, "Tarefa", LocalDate.now().plusDays(1));
        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefa));
        when(tarefaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        TarefaDTO resultado = service.iniciar(1L, 1L);

        assertEquals("EM_EXECUCAO", resultado.getStatus());
    }

    @Test
    void deveConcluirTarefa() {
        Usuario usuario = new Usuario(1L, "Admin", "admin@test.com", "123", UsuarioRole.ADMIN);
        when(usuarioService.findEntityById(1L)).thenReturn(usuario);

        Tarefa tarefa = criarTarefa(1L, "Tarefa", LocalDate.now().plusDays(1));
        tarefa.setStatus(TarefaStatus.EM_EXECUCAO);
        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefa));
        when(tarefaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        TarefaDTO resultado = service.concluir(1L, 1L);

        assertEquals("CONCLUIDA", resultado.getStatus());
        assertNotNull(resultado.getDataConclusao());
    }

    @Test
    void deveRejeitarConclusaoDireta() {
        Tarefa tarefa = criarTarefa(1L, "Tarefa", LocalDate.now().plusDays(1));
        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefa));

        assertThrows(RuntimeException.class, () -> service.concluir(1L, 1L));
    }

    @Test
    void deveExcluirTarefa() {
        doNothing().when(usuarioService).validarRole(anyLong(), any());
        when(tarefaRepository.existsById(1L)).thenReturn(true);

        service.excluir(1L, 1L);

        verify(tarefaRepository).deleteById(1L);
    }

    @Test
    void deveLancarExcecaoAoExcluirTarefaInexistente() {
        doNothing().when(usuarioService).validarRole(anyLong(), any());
        when(tarefaRepository.existsById(99L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> service.excluir(99L, 1L));
    }

    @Test
    void deveRetornarTarefasHoje() {
        Tarefa tarefa = criarTarefa(1L, "Tarefa Hoje", LocalDate.now());
        when(tarefaRepository.findByPrazo(LocalDate.now())).thenReturn(List.of(tarefa));

        List<TarefaDTO> resultado = service.tarefasHoje();

        assertEquals(1, resultado.size());
        assertEquals("Tarefa Hoje", resultado.get(0).getTitulo());
    }

    @Test
    void deveRetornarTarefasSemana() {
        Tarefa tarefa = criarTarefa(1L, "Tarefa Semana", LocalDate.now().plusDays(3));
        when(tarefaRepository.findByPrazoBetween(LocalDate.now(), LocalDate.now().plusDays(6)))
                .thenReturn(List.of(tarefa));

        List<TarefaDTO> resultado = service.tarefasSemana();

        assertEquals(1, resultado.size());
    }

    @Test
    void deveRetornarTarefasAtrasadas() {
        Tarefa tarefa = criarTarefa(1L, "Tarefa Atrasada", LocalDate.now().minusDays(2));
        when(tarefaRepository.findAtrasadas(LocalDate.now())).thenReturn(List.of(tarefa));

        List<TarefaDTO> resultado = service.tarefasAtrasadas();

        assertEquals(1, resultado.size());
    }

    @Test
    void deveRetornarTarefasProximas() {
        Tarefa tarefa = criarTarefa(1L, "Tarefa Proxima", LocalDate.now().plusDays(5));
        when(tarefaRepository.findProximas(LocalDate.now().plusDays(1), LocalDate.now().plusDays(7)))
                .thenReturn(List.of(tarefa));

        List<TarefaDTO> resultado = service.tarefasProximas();

        assertEquals(1, resultado.size());
    }

    @Test
    void deveRetornarResumo() {
        when(tarefaRepository.findByPrazo(LocalDate.now())).thenReturn(List.of());
        when(tarefaRepository.countByPrazo(LocalDate.now())).thenReturn(0L);
        when(tarefaRepository.findAtrasadas(LocalDate.now())).thenReturn(List.of());
        when(tarefaRepository.countAtrasadas(LocalDate.now())).thenReturn(0L);
        when(tarefaRepository.findProximas(any(), any())).thenReturn(List.of());
        when(tarefaRepository.countProximas(any(), any())).thenReturn(0L);

        ResumoTarefasDTO resultado = service.resumo();

        assertNotNull(resultado);
        assertEquals(0, resultado.getContagemHoje());
        assertEquals(0, resultado.getContagemAtrasadas());
        assertEquals(0, resultado.getContagemProximas());
    }

    private Tarefa criarTarefa(Long id, String titulo, LocalDate prazo) {
        Tarefa t = new Tarefa();
        t.setId(id);
        t.setTitulo(titulo);
        t.setDescricao("Descrição");
        t.setCategoria(categoria);
        t.setPrazo(prazo);
        t.setStatus(TarefaStatus.PENDENTE);
        return t;
    }
}
