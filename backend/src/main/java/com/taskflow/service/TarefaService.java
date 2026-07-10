package com.taskflow.service;

import com.taskflow.config.RegraNegocioException;
import com.taskflow.config.StatusInvalidoException;
import com.taskflow.config.TarefaNotFoundException;
import com.taskflow.dto.ResumoTarefasDTO;
import com.taskflow.dto.TarefaDTO;
import com.taskflow.model.*;
import com.taskflow.repository.TarefaRepository;
import com.taskflow.repository.TarefaSpecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TarefaService {

    private static final Logger log = LoggerFactory.getLogger(TarefaService.class);

    private final TarefaRepository tarefaRepository;
    private final CategoriaService categoriaService;
    private final UsuarioService usuarioService;
    private final HistoricoService historicoService;
    private final NotificacaoService notificacaoService;
    private final AutomacaoService automacaoService;

    public TarefaService(TarefaRepository tarefaRepository, CategoriaService categoriaService, UsuarioService usuarioService,
                         HistoricoService historicoService, NotificacaoService notificacaoService,
                         AutomacaoService automacaoService) {
        this.tarefaRepository = tarefaRepository;
        this.categoriaService = categoriaService;
        this.usuarioService = usuarioService;
        this.historicoService = historicoService;
        this.notificacaoService = notificacaoService;
        this.automacaoService = automacaoService;
    }

    public Page<TarefaDTO> listar(String status, Long categoriaId, Long responsavelId,
                                   String prioridade, String busca, int pagina, int tamanho) {
        PageRequest pageRequest = PageRequest.of(pagina, tamanho);

        TarefaStatus statusEnum = status != null && !status.isBlank() ? TarefaStatus.valueOf(status) : null;
        Prioridade prioridadeEnum = prioridade != null && !prioridade.isBlank() ? Prioridade.valueOf(prioridade) : null;

        Specification<Tarefa> spec = Specification
                .where(TarefaSpecs.comStatus(statusEnum))
                .and(TarefaSpecs.comCategoriaId(categoriaId))
                .and(TarefaSpecs.comResponsavelId(responsavelId))
                .and(TarefaSpecs.comPrioridade(prioridadeEnum))
                .and(TarefaSpecs.comBusca(busca));

        Page<Tarefa> page = tarefaRepository.findAll(spec, pageRequest);
        return page.map(this::toDTO);
    }

    public TarefaDTO buscarPorId(Long id) {
        return tarefaRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new TarefaNotFoundException(id));
    }

    public TarefaDTO criar(TarefaDTO dto, Long usuarioId) {
        if (usuarioId != null) {
            usuarioService.validarRole(usuarioId, com.taskflow.model.UsuarioRole.DISTRIBUIDOR);
        }
        Tarefa tarefa = toEntity(dto);
        tarefa.setStatus(TarefaStatus.PENDENTE);
        tarefa.setDistribuidorId(usuarioId);
        TarefaDTO saved = toDTO(tarefaRepository.save(tarefa));
        log.info("Usuario {} criou tarefa {}", usuarioId, saved.getId());
        automacaoService.avaliarEvento("TAREFA_CRIADA", tarefa);
        return saved;
    }

    public TarefaDTO distribuir(Long id, Long responsavelId, Long distribuidorId) {
        usuarioService.validarRole(distribuidorId, com.taskflow.model.UsuarioRole.DISTRIBUIDOR);
        Tarefa tarefa = tarefaRepository.findById(id)
                .orElseThrow(() -> new TarefaNotFoundException(id));
        if (tarefa.getStatus() == TarefaStatus.CONCLUIDA || tarefa.getStatus() == TarefaStatus.ARQUIVADA) {
            throw new RegraNegocioException("Não é possível distribuir uma tarefa concluída ou arquivada");
        }

        String anterior = tarefa.getResponsavel() != null ? tarefa.getResponsavel().getNome() : "Não atribuído";
        Usuario responsavel = usuarioService.findEntityById(responsavelId);
        tarefa.setResponsavel(responsavel);
        tarefa.setDistribuidorId(distribuidorId);
        TarefaDTO saved = toDTO(tarefaRepository.save(tarefa));

        Usuario distribuidor = usuarioService.findEntityById(distribuidorId);
        historicoService.registrar("responsavel", anterior, responsavel.getNome(), tarefa, distribuidor);
        notificacaoService.criar(
                "Você recebeu a tarefa \"" + tarefa.getTitulo() + "\" de " + distribuidor.getNome(),
                responsavelId, tarefa.getId(), "ATRIBUICAO"
        );

        log.info("Usuario {} distribuiu tarefa {} para usuario {}", distribuidorId, id, responsavelId);
        return saved;
    }

    public TarefaDTO atualizar(Long id, TarefaDTO dto, Long usuarioId) {
        usuarioService.validarRole(usuarioId, com.taskflow.model.UsuarioRole.DISTRIBUIDOR);
        Tarefa tarefa = tarefaRepository.findById(id)
                .orElseThrow(() -> new TarefaNotFoundException(id));
        if (tarefa.getStatus() == TarefaStatus.CONCLUIDA) {
            throw new RegraNegocioException("Não é possível editar uma tarefa concluída");
        }

        Usuario usuario = usuarioService.findEntityById(usuarioId);

        if (!dto.getTitulo().equals(tarefa.getTitulo())) {
            historicoService.registrar("titulo", tarefa.getTitulo(), dto.getTitulo(), tarefa, usuario);
        }
        if (dto.getDescricao() != null && !dto.getDescricao().equals(tarefa.getDescricao())) {
            historicoService.registrar("descricao", tarefa.getDescricao(), dto.getDescricao(), tarefa, usuario);
        }
        if (dto.getPrazo() != null && !dto.getPrazo().equals(tarefa.getPrazo())) {
            historicoService.registrar("prazo", String.valueOf(tarefa.getPrazo()), String.valueOf(dto.getPrazo()), tarefa, usuario);
        }

        tarefa.setTitulo(dto.getTitulo());
        tarefa.setDescricao(dto.getDescricao());
        tarefa.setPrazo(dto.getPrazo());

        if (dto.getPrioridade() != null && !dto.getPrioridade().equals(tarefa.getPrioridade().name())) {
            historicoService.registrar("prioridade", tarefa.getPrioridade().name(), dto.getPrioridade(), tarefa, usuario);
            tarefa.setPrioridade(Prioridade.valueOf(dto.getPrioridade()));
        }

        if (dto.getCategoriaId() != null && !dto.getCategoriaId().equals(tarefa.getCategoria().getId())) {
            String catAnterior = tarefa.getCategoria().getNome();
            Categoria categoria = categoriaService.findEntityById(dto.getCategoriaId());
            tarefa.setCategoria(categoria);
            historicoService.registrar("categoria", catAnterior, categoria.getNome(), tarefa, usuario);
        }

        TarefaDTO saved = toDTO(tarefaRepository.save(tarefa));
        log.info("Usuario {} atualizou tarefa {}", usuarioId, id);
        return saved;
    }

    public TarefaDTO iniciar(Long id, Long usuarioId) {
        Tarefa tarefa = tarefaRepository.findById(id)
                .orElseThrow(() -> new TarefaNotFoundException(id));
        if (tarefa.getResponsavel() != null && !tarefa.getResponsavel().getId().equals(usuarioId)) {
            usuarioService.validarRole(usuarioId, com.taskflow.model.UsuarioRole.DISTRIBUIDOR);
        }
        validarTransicao(tarefa.getStatus(), TarefaStatus.EM_EXECUCAO);
        String anterior = tarefa.getStatus().name();
        tarefa.setStatus(TarefaStatus.EM_EXECUCAO);
        TarefaDTO saved = toDTO(tarefaRepository.save(tarefa));
        Usuario usuario = usuarioService.findEntityById(usuarioId);
        historicoService.registrar("status", anterior, "EM_EXECUCAO", tarefa, usuario);
        log.info("Usuario {} iniciou tarefa {}", usuarioId, id);
        return saved;
    }

    public TarefaDTO concluir(Long id, Long usuarioId) {
        Tarefa tarefa = tarefaRepository.findById(id)
                .orElseThrow(() -> new TarefaNotFoundException(id));
        if (tarefa.getResponsavel() != null && !tarefa.getResponsavel().getId().equals(usuarioId)) {
            usuarioService.validarRole(usuarioId, com.taskflow.model.UsuarioRole.DISTRIBUIDOR);
        }
        validarTransicao(tarefa.getStatus(), TarefaStatus.CONCLUIDA);
        String anterior = tarefa.getStatus().name();
        tarefa.setStatus(TarefaStatus.CONCLUIDA);
        tarefa.setDataConclusao(LocalDateTime.now());
        TarefaDTO saved = toDTO(tarefaRepository.save(tarefa));
        Usuario usuario = usuarioService.findEntityById(usuarioId);
        historicoService.registrar("status", anterior, "CONCLUIDA", tarefa, usuario);
        if (tarefa.getResponsavel() != null && !tarefa.getResponsavel().getId().equals(usuarioId)) {
            notificacaoService.criar(
                    "Tarefa \"" + tarefa.getTitulo() + "\" foi concluída por " + usuario.getNome(),
                    tarefa.getResponsavel().getId(), tarefa.getId(), "CONCLUSAO"
            );
        }
        log.info("Usuario {} concluiu tarefa {}", usuarioId, id);
        automacaoService.avaliarEvento("STATUS_MUDOU", tarefa);
        return saved;
    }

    public void excluir(Long id, Long usuarioId) {
        usuarioService.validarRole(usuarioId, com.taskflow.model.UsuarioRole.ADMIN);
        if (!tarefaRepository.existsById(id)) {
            throw new TarefaNotFoundException(id);
        }
        tarefaRepository.deleteById(id);
        log.info("Usuario {} excluiu tarefa {}", usuarioId, id);
    }

    public List<TarefaDTO> tarefasHoje() {
        return tarefaRepository.findByPrazo(LocalDate.now()).stream().map(this::toDTO).toList();
    }

    public List<TarefaDTO> tarefasSemana() {
        LocalDate hoje = LocalDate.now();
        return tarefaRepository.findByPrazoBetween(hoje, hoje.plusDays(6)).stream().map(this::toDTO).toList();
    }

    public List<TarefaDTO> tarefasAtrasadas() {
        return tarefaRepository.findAtrasadas(LocalDate.now()).stream().map(this::toDTO).toList();
    }

    public List<TarefaDTO> tarefasProximas() {
        LocalDate hoje = LocalDate.now();
        return tarefaRepository.findProximas(hoje.plusDays(1), hoje.plusDays(7)).stream().map(this::toDTO).toList();
    }

    public ResumoTarefasDTO resumo() {
        LocalDate hoje = LocalDate.now();
        ResumoTarefasDTO dto = new ResumoTarefasDTO();
        dto.setHoje(tarefasHoje());
        dto.setContagemHoje(tarefaRepository.countByPrazo(hoje));
        dto.setAtrasadas(tarefasAtrasadas());
        dto.setContagemAtrasadas(tarefaRepository.countAtrasadas(hoje));
        dto.setProximas(tarefasProximas());
        dto.setContagemProximas(tarefaRepository.countProximas(hoje.plusDays(1), hoje.plusDays(7)));
        return dto;
    }

    private void validarTransicao(TarefaStatus atual, TarefaStatus novo) {
        boolean transicaoValida = switch (atual) {
            case PENDENTE -> novo == TarefaStatus.EM_EXECUCAO;
            case EM_EXECUCAO -> novo == TarefaStatus.CONCLUIDA || novo == TarefaStatus.PENDENTE;
            case CONCLUIDA -> false;
            case ARQUIVADA -> false;
        };
        if (!transicaoValida) {
            throw new StatusInvalidoException(
                    String.format("Transição de status inválida: %s → %s", atual, novo));
        }
    }

    private TarefaDTO toDTO(Tarefa t) {
        TarefaDTO dto = new TarefaDTO();
        dto.setId(t.getId());
        dto.setTitulo(t.getTitulo());
        dto.setDescricao(t.getDescricao());
        dto.setCategoriaId(t.getCategoria().getId());
        dto.setCategoriaNome(t.getCategoria().getNome());
        dto.setPrazo(t.getPrazo());
        dto.setStatus(t.getStatus().name());
        dto.setPrioridade(t.getPrioridade().name());
        dto.setDataCriacao(t.getDataCriacao());
        dto.setDataConclusao(t.getDataConclusao());
        if (t.getResponsavel() != null) {
            dto.setResponsavelId(t.getResponsavel().getId());
            dto.setResponsavelNome(t.getResponsavel().getNome());
        }
        dto.setDistribuidorId(t.getDistribuidorId());
        return dto;
    }

    private Tarefa toEntity(TarefaDTO dto) {
        Tarefa tarefa = new Tarefa();
        tarefa.setTitulo(dto.getTitulo());
        tarefa.setDescricao(dto.getDescricao());
        tarefa.setPrazo(dto.getPrazo());
        tarefa.setStatus(dto.getStatus() != null ? TarefaStatus.valueOf(dto.getStatus()) : TarefaStatus.PENDENTE);
        tarefa.setPrioridade(dto.getPrioridade() != null ? Prioridade.valueOf(dto.getPrioridade()) : Prioridade.MEDIA);

        if (dto.getCategoriaId() != null) {
            Categoria categoria = categoriaService.findEntityById(dto.getCategoriaId());
            tarefa.setCategoria(categoria);
        }

        if (dto.getResponsavelId() != null) {
            Usuario responsavel = usuarioService.findEntityById(dto.getResponsavelId());
            tarefa.setResponsavel(responsavel);
        }

        return tarefa;
    }
}
