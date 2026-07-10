package com.taskflow.service;

import com.taskflow.dto.TarefaRecorrenteDTO;
import com.taskflow.model.*;
import com.taskflow.repository.CategoriaRepository;
import com.taskflow.repository.TarefaRecorrenteRepository;
import com.taskflow.repository.TarefaRepository;
import com.taskflow.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TarefaRecorrenteService {

    private static final Logger log = LoggerFactory.getLogger(TarefaRecorrenteService.class);

    private final TarefaRecorrenteRepository repository;
    private final TarefaRepository tarefaRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;

    public TarefaRecorrenteService(TarefaRecorrenteRepository repository,
                                   TarefaRepository tarefaRepository,
                                   CategoriaRepository categoriaRepository,
                                   UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.tarefaRepository = tarefaRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<TarefaRecorrenteDTO> listarTodas() {
        return repository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TarefaRecorrenteDTO> listarPorCriador(Long criadorId) {
        return repository.findByCriadorId(criadorId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public TarefaRecorrenteDTO criar(TarefaRecorrenteDTO dto, Long criadorId) {
        Tarefa modelo = tarefaRepository.findById(dto.getTarefaModeloId())
                .orElseThrow(() -> new RuntimeException("Tarefa modelo não encontrada com id: " + dto.getTarefaModeloId()));
        Usuario criador = usuarioRepository.findById(criadorId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com id: " + criadorId));

        TarefaRecorrente recorrente = new TarefaRecorrente();
        recorrente.setTarefaModelo(modelo);
        recorrente.setRecorrencia(dto.getRecorrencia());
        recorrente.setConfiguracao(dto.getConfiguracao());
        recorrente.setProximaExecucao(dto.getProximaExecucao());
        recorrente.setAtiva(true);
        recorrente.setCriador(criador);

        TarefaRecorrente salva = repository.save(recorrente);
        log.info("Tarefa recorrente criada: id={}, modelo={}, recorrência={}", salva.getId(), modelo.getTitulo(), dto.getRecorrencia());
        return toDTO(salva);
    }

    @Transactional
    public TarefaRecorrenteDTO atualizar(Long id, TarefaRecorrenteDTO dto) {
        TarefaRecorrente recorrente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarefa recorrente não encontrada com id: " + id));

        if (dto.getRecorrencia() != null) recorrente.setRecorrencia(dto.getRecorrencia());
        if (dto.getConfiguracao() != null) recorrente.setConfiguracao(dto.getConfiguracao());
        if (dto.getProximaExecucao() != null) recorrente.setProximaExecucao(dto.getProximaExecucao());
        if (dto.getAtiva() != null) recorrente.setAtiva(dto.getAtiva());

        TarefaRecorrente atualizada = repository.save(recorrente);
        return toDTO(atualizada);
    }

    @Transactional
    public void excluir(Long id) {
        TarefaRecorrente recorrente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarefa recorrente não encontrada com id: " + id));
        repository.delete(recorrente);
    }

    @Transactional
    public TarefaRecorrenteDTO pular(Long id) {
        TarefaRecorrente recorrente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarefa recorrente não encontrada com id: " + id));

        recorrente.setProximaExecucao(calcularProxima(recorrente.getProximaExecucao(), recorrente.getRecorrencia()));
        TarefaRecorrente atualizada = repository.save(recorrente);
        log.info("Próxima execução pulada: id={}, nova data={}", id, atualizada.getProximaExecucao());
        return toDTO(atualizada);
    }

    @Transactional
    public TarefaRecorrenteDTO adiar(Long id, int dias) {
        TarefaRecorrente recorrente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarefa recorrente não encontrada com id: " + id));

        recorrente.setProximaExecucao(recorrente.getProximaExecucao().plusDays(dias));
        TarefaRecorrente atualizada = repository.save(recorrente);
        log.info("Tarefa recorrente adiada: id={}, {} dias, nova data={}", id, dias, atualizada.getProximaExecucao());
        return toDTO(atualizada);
    }

    @Transactional
    public List<Tarefa> executarPendentes() {
        LocalDate hoje = LocalDate.now();
        List<TarefaRecorrente> pendentes = repository.findByProximaExecucaoLessThanEqualAndAtivaTrue(hoje);
        List<Tarefa> criadas = new ArrayList<>();

        for (TarefaRecorrente recorrente : pendentes) {
            Tarefa novaTarefa = criarTarefaDaRecorrente(recorrente);
            tarefaRepository.save(novaTarefa);
            criadas.add(novaTarefa);

            recorrente.setProximaExecucao(calcularProxima(hoje, recorrente.getRecorrencia()));
            repository.save(recorrente);
            log.info("Tarefa recorrente executada: modelo={}, nova tarefa={}, próxima={}",
                    recorrente.getTarefaModelo().getTitulo(), novaTarefa.getId(), recorrente.getProximaExecucao());
        }
        return criadas;
    }

    @Transactional(readOnly = true)
    public List<LocalDate> calcularProximasExecucoes(Long id, int quantidade) {
        TarefaRecorrente recorrente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarefa recorrente não encontrada com id: " + id));

        List<LocalDate> datas = new ArrayList<>();
        LocalDate atual = recorrente.getProximaExecucao();
        for (int i = 0; i < quantidade; i++) {
            datas.add(atual);
            atual = calcularProxima(atual, recorrente.getRecorrencia());
        }
        return datas;
    }

    private Tarefa criarTarefaDaRecorrente(TarefaRecorrente recorrente) {
        Tarefa modelo = recorrente.getTarefaModelo();
        Tarefa nova = new Tarefa();
        nova.setTitulo(modelo.getTitulo());
        nova.setDescricao(modelo.getDescricao());
        nova.setCategoria(modelo.getCategoria());
        nova.setPrazo(recorrente.getProximaExecucao());
        nova.setPrioridade(modelo.getPrioridade() != null ? modelo.getPrioridade() : Prioridade.MEDIA);
        nova.setStatus(TarefaStatus.PENDENTE);
        return nova;
    }

    private LocalDate calcularProxima(LocalDate atual, Recorrencia recorrencia) {
        switch (recorrencia) {
            case DIARIA:
                return atual.plusDays(1);
            case SEMANAL:
                return atual.plusWeeks(1);
            case QUINZENAL:
                return atual.plusWeeks(2);
            case MENSAL:
                return atual.plusMonths(1);
            case BIMESTRAL:
                return atual.plusMonths(2);
            case PERSONALIZADA:
                return atual.plusWeeks(1);
            default:
                return atual.plusDays(1);
        }
    }

    private TarefaRecorrenteDTO toDTO(TarefaRecorrente r) {
        TarefaRecorrenteDTO dto = new TarefaRecorrenteDTO();
        dto.setId(r.getId());
        dto.setTarefaModeloId(r.getTarefaModelo().getId());
        dto.setTarefaModeloTitulo(r.getTarefaModelo().getTitulo());
        if (r.getTarefaModelo().getCategoria() != null) {
            dto.setCategoriaId(r.getTarefaModelo().getCategoria().getId());
            dto.setCategoriaNome(r.getTarefaModelo().getCategoria().getNome());
        }
        dto.setDescricao(r.getTarefaModelo().getDescricao());
        dto.setRecorrencia(r.getRecorrencia());
        dto.setConfiguracao(r.getConfiguracao());
        dto.setProximaExecucao(r.getProximaExecucao());
        dto.setAtiva(r.getAtiva());
        dto.setDataCriacao(r.getDataCriacao());
        if (r.getCriador() != null) {
            dto.setCriadorId(r.getCriador().getId());
            dto.setCriadorNome(r.getCriador().getNome());
        }
        return dto;
    }
}
