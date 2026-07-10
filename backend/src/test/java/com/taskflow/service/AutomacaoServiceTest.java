package com.taskflow.service;

import com.taskflow.dto.RegraAutomacaoDTO;
import com.taskflow.dto.TarefaDTO;
import com.taskflow.model.*;
import com.taskflow.repository.RegraAutomacaoRepository;
import com.taskflow.repository.TarefaRepository;
import com.taskflow.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class AutomacaoServiceTest {

    @Mock private RegraAutomacaoRepository regraRepository;
    @Mock private TarefaRepository tarefaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private NotificacaoService notificacaoService;
    @Mock private TarefaService tarefaService;

    private AutomacaoService service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private Usuario criador;
    private Tarefa tarefa;

    @BeforeEach
    void setUp() {
        service = new AutomacaoService(regraRepository, tarefaRepository, usuarioRepository,
                notificacaoService, tarefaService, objectMapper);

        criador = new Usuario(1L, "Joao", "joao@test.com", "123", UsuarioRole.EXECUTOR);

        Categoria cat = new Categoria(1L, "Trabalho");
        tarefa = new Tarefa();
        tarefa.setId(1L);
        tarefa.setTitulo("Tarefa Teste");
        tarefa.setStatus(TarefaStatus.EM_EXECUCAO);
        tarefa.setPrioridade(Prioridade.MEDIA);
        tarefa.setCategoria(cat);
        tarefa.setPrazo(java.time.LocalDate.now().plusDays(5));
        tarefa.setDistribuidorId(1L);
    }

    @Test
    void deveCriarRegra() {
        RegraAutomacaoDTO dto = new RegraAutomacaoDTO();
        dto.setNome("Notificar ao concluir");
        dto.setCondicao("{\"tipo\":\"STATUS_MUDOU\",\"valor\":\"CONCLUIDA\"}");
        dto.setAcao("{\"tipo\":\"ENVIAR_NOTIFICACAO\",\"dados\":{\"usuarioId\":1,\"mensagem\":\"Tarefa concluída!\"}}");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(criador));
        when(regraRepository.save(any())).thenAnswer(i -> {
            RegraAutomacao r = i.getArgument(0);
            r.setId(1L);
            return r;
        });

        RegraAutomacaoDTO resultado = service.criar(dto, 1L);

        assertEquals("Notificar ao concluir", resultado.getNome());
        assertTrue(resultado.getAtiva());
    }

    @Test
    void deveListarTodas() {
        RegraAutomacao regra = new RegraAutomacao();
        regra.setId(1L);
        regra.setNome("Regra Teste");
        when(regraRepository.findAll()).thenReturn(List.of(regra));

        List<RegraAutomacaoDTO> resultado = service.listarTodas();

        assertEquals(1, resultado.size());
        assertEquals("Regra Teste", resultado.get(0).getNome());
    }

    @Test
    void deveExcluirRegra() {
        RegraAutomacao regra = new RegraAutomacao();
        regra.setId(1L);
        when(regraRepository.findById(1L)).thenReturn(Optional.of(regra));

        service.excluir(1L);

        verify(regraRepository).delete(regra);
    }

    @Test
    void deveToggleAtiva() {
        RegraAutomacao regra = new RegraAutomacao();
        regra.setId(1L);
        regra.setAtiva(true);
        when(regraRepository.findById(1L)).thenReturn(Optional.of(regra));
        when(regraRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.toggleAtiva(1L, false);

        verify(regraRepository).save(argThat(r -> !r.getAtiva()));
    }

    @Test
    void deveAvaliarEventoTarefaCriada() {
        when(regraRepository.findByAtivaTrue()).thenReturn(List.of());

        service.avaliarEvento("TAREFA_CRIADA", tarefa);

        verify(regraRepository).findByAtivaTrue();
    }

    @Test
    void deveExecutarAcaoNotificacao() throws Exception {
        RegraAutomacao regra = new RegraAutomacao();
        regra.setId(1L);
        regra.setNome("Notificar");
        regra.setCondicao("{\"tipo\":\"STATUS_MUDOU\",\"valor\":\"EM_EXECUCAO\"}");
        regra.setAcao("{\"tipo\":\"ENVIAR_NOTIFICACAO\",\"dados\":{\"usuarioId\":1,\"mensagem\":\"Em andamento\"}}");
        regra.setCriador(criador);

        when(regraRepository.findByAtivaTrue()).thenReturn(List.of(regra));

        service.avaliarEvento("STATUS_MUDOU", tarefa);

        verify(notificacaoService).criar("Em andamento", 1L, 1L, "AUTOMACAO");
    }

    @Test
    void deveExecutarAcaoCriarTarefa() throws Exception {
        RegraAutomacao regra = new RegraAutomacao();
        regra.setId(1L);
        regra.setNome("Criar follow-up");
        regra.setCondicao("{\"tipo\":\"STATUS_MUDOU\",\"valor\":\"CONCLUIDA\"}");
        regra.setAcao("{\"tipo\":\"CRIAR_TAREFA\",\"dados\":{\"titulo\":\"Follow-up\",\"diasPrazo\":7,\"prioridade\":\"BAIXA\",\"categoriaId\":1}}");
        regra.setCriador(criador);

        tarefa.setStatus(TarefaStatus.CONCLUIDA);

        when(regraRepository.findByAtivaTrue()).thenReturn(List.of(regra));

        service.avaliarEvento("STATUS_MUDOU", tarefa);

        verify(tarefaService).criar(any(TarefaDTO.class), eq(1L));
    }

    @Test
    void naoDeveExecutarRegraInativa() throws Exception {
        RegraAutomacao regra = new RegraAutomacao();
        regra.setId(1L);
        regra.setNome("Inativa");
        regra.setCondicao("{\"tipo\":\"STATUS_MUDOU\",\"valor\":\"CONCLUIDA\"}");
        regra.setAcao("{\"tipo\":\"ENVIAR_NOTIFICACAO\",\"dados\":{\"usuarioId\":1,\"mensagem\":\"Teste\"}}");
        regra.setAtiva(false);

        when(regraRepository.findByAtivaTrue()).thenReturn(List.of());

        service.avaliarEvento("STATUS_MUDOU", tarefa);

        verify(notificacaoService, never()).criar(anyString(), anyLong(), anyLong(), anyString());
    }
}
