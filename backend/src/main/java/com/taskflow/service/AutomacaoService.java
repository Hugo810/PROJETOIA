package com.taskflow.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.dto.RegraAutomacaoDTO;
import com.taskflow.dto.TarefaDTO;
import com.taskflow.model.*;
import com.taskflow.repository.RegraAutomacaoRepository;
import com.taskflow.repository.TarefaRepository;
import com.taskflow.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class AutomacaoService {

    private static final Logger log = LoggerFactory.getLogger(AutomacaoService.class);

    private final RegraAutomacaoRepository regraRepository;
    private final TarefaRepository tarefaRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacaoService notificacaoService;
    private final TarefaService tarefaService;
    private final ObjectMapper objectMapper;

    public AutomacaoService(RegraAutomacaoRepository regraRepository, TarefaRepository tarefaRepository,
                            UsuarioRepository usuarioRepository, NotificacaoService notificacaoService,
                            @Lazy TarefaService tarefaService, ObjectMapper objectMapper) {
        this.regraRepository = regraRepository;
        this.tarefaRepository = tarefaRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacaoService = notificacaoService;
        this.tarefaService = tarefaService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<RegraAutomacaoDTO> listarTodas() {
        return regraRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<RegraAutomacaoDTO> listarPorCriador(Long criadorId) {
        return regraRepository.findByCriadorId(criadorId).stream().map(this::toDTO).toList();
    }

    @Transactional
    public RegraAutomacaoDTO criar(RegraAutomacaoDTO dto, Long criadorId) {
        Usuario criador = usuarioRepository.findById(criadorId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com id: " + criadorId));

        RegraAutomacao regra = new RegraAutomacao();
        regra.setNome(dto.getNome());
        regra.setCondicao(dto.getCondicao());
        regra.setAcao(dto.getAcao());
        regra.setAtiva(true);
        regra.setCriador(criador);

        RegraAutomacao salva = regraRepository.save(regra);
        log.info("Regra de automação criada: id={}, nome={}", salva.getId(), dto.getNome());
        return toDTO(salva);
    }

    @Transactional
    public RegraAutomacaoDTO atualizar(Long id, RegraAutomacaoDTO dto) {
        RegraAutomacao regra = regraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Regra não encontrada com id: " + id));

        if (dto.getNome() != null) regra.setNome(dto.getNome());
        if (dto.getCondicao() != null) regra.setCondicao(dto.getCondicao());
        if (dto.getAcao() != null) regra.setAcao(dto.getAcao());
        if (dto.getAtiva() != null) regra.setAtiva(dto.getAtiva());

        RegraAutomacao atualizada = regraRepository.save(regra);
        return toDTO(atualizada);
    }

    @Transactional
    public void excluir(Long id) {
        RegraAutomacao regra = regraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Regra não encontrada com id: " + id));
        regraRepository.delete(regra);
    }

    @Transactional
    public void toggleAtiva(Long id, Boolean ativa) {
        RegraAutomacao regra = regraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Regra não encontrada com id: " + id));
        regra.setAtiva(ativa);
        regraRepository.save(regra);
    }

    @Transactional
    public void avaliarEvento(String evento, Tarefa tarefa) {
        List<RegraAutomacao> regras = regraRepository.findByAtivaTrue();
        for (RegraAutomacao regra : regras) {
            try {
                if (avaliarCondicao(regra.getCondicao(), evento, tarefa)) {
                    executarAcao(regra.getAcao(), tarefa, regra);
                    log.info("Regra '{}' executada para tarefa {}", regra.getNome(), tarefa.getId());
                }
            } catch (Exception e) {
                log.error("Erro ao avaliar regra {}: {}", regra.getId(), e.getMessage());
            }
        }
    }

    private boolean avaliarCondicao(String condicaoJson, String evento, Tarefa tarefa) throws Exception {
        Map<String, Object> condicao = objectMapper.readValue(condicaoJson, new TypeReference<>() {});
        String tipoCondicao = (String) condicao.get("tipo");

        return switch (TipoCondicao.valueOf(tipoCondicao)) {
            case STATUS_MUDOU -> {
                String valorEsperado = (String) condicao.get("valor");
                String statusAnterior = (String) condicao.getOrDefault("statusAnterior", null);
                yield tarefa.getStatus().name().equals(valorEsperado);
            }
            case PRAZO_CHEGOU -> {
                String valorEsperado = (String) condicao.getOrDefault("valor", "HOJE");
                yield "HOJE".equals(valorEsperado);
            }
            case PRAZO_ATRASADO -> {
                yield java.time.LocalDate.now().isAfter(tarefa.getPrazo()) &&
                       tarefa.getStatus() != TarefaStatus.CONCLUIDA;
            }
            case TAREFA_CRIADA -> "TAREFA_CRIADA".equals(evento);
            case PRIORIDADE_ALTA -> tarefa.getPrioridade() == Prioridade.ALTA;
            case CATEGORIA_IGUAL -> {
                Long catId = ((Number) condicao.get("valor")).longValue();
                yield tarefa.getCategoria() != null && tarefa.getCategoria().getId().equals(catId);
            }
        };
    }

    private void executarAcao(String acaoJson, Tarefa tarefa, RegraAutomacao regra) throws Exception {
        Map<String, Object> acao = objectMapper.readValue(acaoJson, new TypeReference<>() {});
        String tipoAcao = (String) acao.get("tipo");
        Map<String, Object> dados = (Map<String, Object>) acao.get("dados");

        switch (TipoAcao.valueOf(tipoAcao)) {
            case CRIAR_TAREFA -> {
                TarefaDTO novaTarefa = new TarefaDTO();
                novaTarefa.setTitulo((String) dados.get("titulo"));
                novaTarefa.setDescricao((String) dados.getOrDefault("descricao", ""));
                novaTarefa.setPrazo(java.time.LocalDate.now().plusDays(
                        dados.get("diasPrazo") != null ? ((Number) dados.get("diasPrazo")).intValue() : 7));
                novaTarefa.setPrioridade(dados.get("prioridade") != null ? (String) dados.get("prioridade") : "MEDIA");
                Long catId = dados.get("categoriaId") != null ? ((Number) dados.get("categoriaId")).longValue() : tarefa.getCategoria().getId();
                novaTarefa.setCategoriaId(catId);
                tarefaService.criar(novaTarefa, regra.getCriador() != null ? regra.getCriador().getId() : null);
            }
            case ENVIAR_NOTIFICACAO -> {
                Long usuarioId = ((Number) dados.get("usuarioId")).longValue();
                String mensagem = (String) dados.get("mensagem");
                notificacaoService.criar(mensagem, usuarioId, tarefa.getId(), "AUTOMACAO");
            }
            case ATRIBUIR_RESPONSAVEL -> {
                Long responsavelId = ((Number) dados.get("usuarioId")).longValue();
                Long distribuidorId = regra.getCriador() != null ? regra.getCriador().getId() : null;
                if (distribuidorId == null) distribuidorId = responsavelId;
                tarefaService.distribuir(tarefa.getId(), responsavelId, distribuidorId);
            }
            case ALTERAR_PRIORIDADE -> {
                String novaPrioridade = (String) dados.get("prioridade");
                TarefaDTO dto = new TarefaDTO();
                dto.setTitulo(tarefa.getTitulo());
                dto.setDescricao(tarefa.getDescricao());
                dto.setPrazo(tarefa.getPrazo());
                dto.setPrioridade(novaPrioridade);
                dto.setCategoriaId(tarefa.getCategoria().getId());
                if (regra.getCriador() != null) {
                    tarefaService.atualizar(tarefa.getId(), dto, regra.getCriador().getId());
                }
            }
            case ENVIAR_NOTIFICACAO_CRIADOR -> {
                Long criadorId = tarefa.getDistribuidorId();
                if (criadorId != null) {
                    String msg = (String) dados.getOrDefault("mensagem",
                            "Automação: tarefa \"" + tarefa.getTitulo() + "\" foi processada");
                    notificacaoService.criar(msg, criadorId, tarefa.getId(), "AUTOMACAO");
                }
            }
        };
    }

    private RegraAutomacaoDTO toDTO(RegraAutomacao r) {
        RegraAutomacaoDTO dto = new RegraAutomacaoDTO();
        dto.setId(r.getId());
        dto.setNome(r.getNome());
        dto.setCondicao(r.getCondicao());
        dto.setAcao(r.getAcao());
        dto.setAtiva(r.getAtiva());
        dto.setDataCriacao(r.getDataCriacao());
        if (r.getCriador() != null) {
            dto.setCriadorId(r.getCriador().getId());
            dto.setCriadorNome(r.getCriador().getNome());
        }
        return dto;
    }
}
