package com.taskflow.service;

import com.taskflow.dto.*;
import com.taskflow.model.*;
import com.taskflow.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GanttService {

    private static final Logger log = LoggerFactory.getLogger(GanttService.class);

    private final TarefaRepository tarefaRepository;
    private final DependenciaTarefaRepository dependenciaRepository;
    private final MarcoRepository marcoRepository;
    private final UsuarioRepository usuarioRepository;

    public GanttService(TarefaRepository tarefaRepository, DependenciaTarefaRepository dependenciaRepository,
                        MarcoRepository marcoRepository, UsuarioRepository usuarioRepository) {
        this.tarefaRepository = tarefaRepository;
        this.dependenciaRepository = dependenciaRepository;
        this.marcoRepository = marcoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<GanttTarefa> buscarTarefas(LocalDate inicio, LocalDate fim) {
        List<Tarefa> tarefas;
        if (inicio != null && fim != null) {
            tarefas = tarefaRepository.findByPrazoBetween(inicio, fim);
        } else {
            LocalDate hoje = LocalDate.now();
            tarefas = tarefaRepository.findByPrazoBetween(hoje.minusDays(30), hoje.plusDays(60));
        }

        return tarefas.stream().map(t -> {
            GanttTarefa dto = new GanttTarefa();
            dto.setId(t.getId());
            dto.setTitulo(t.getTitulo());
            dto.setPrazo(t.getPrazo());
            dto.setStatus(t.getStatus().name());
            dto.setPrioridade(t.getPrioridade().name());
            if (t.getResponsavel() != null) {
                dto.setResponsavel(t.getResponsavel().getNome());
                dto.setResponsavelId(t.getResponsavel().getId());
            }
            if (t.getCategoria() != null) {
                dto.setCategoria(t.getCategoria().getNome());
            }
            long duracao = t.getDataCriacao() != null ?
                    java.time.temporal.ChronoUnit.DAYS.between(t.getDataCriacao().toLocalDate(), t.getPrazo()) : 3;
            dto.setDuracaoDias(Math.max(duracao, 1));
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DependenciaTarefaDTO> listarDependencias(Long tarefaId) {
        List<DependenciaTarefa> deps = tarefaId != null ?
                dependenciaRepository.findByTarefaId(tarefaId) :
                dependenciaRepository.findAll();
        return deps.stream().map(this::toDependenciaDTO).collect(Collectors.toList());
    }

    @Transactional
    public DependenciaTarefaDTO criarDependencia(Long tarefaId, Long tarefaDependenteId, TipoDependencia tipo) {
        Tarefa tarefa = tarefaRepository.findById(tarefaId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada com id: " + tarefaId));
        Tarefa dependente = tarefaRepository.findById(tarefaDependenteId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada com id: " + tarefaDependenteId));

        if (tarefaId.equals(tarefaDependenteId)) {
            throw new RuntimeException("Uma tarefa não pode depender de si mesma");
        }

        DependenciaTarefa dep = new DependenciaTarefa();
        dep.setTarefa(tarefa);
        dep.setTarefaDependente(dependente);
        dep.setTipo(tipo);

        DependenciaTarefa salva = dependenciaRepository.save(dep);
        log.info("Dependência criada: tarefa {} {} tarefa {}", tarefaId, tipo, tarefaDependenteId);
        return toDependenciaDTO(salva);
    }

    @Transactional
    public void excluirDependencia(Long id) {
        dependenciaRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<MarcoDTO> listarMarcos() {
        return marcoRepository.findAllByOrderByDataAsc().stream()
                .map(this::toMarcoDTO).collect(Collectors.toList());
    }

    @Transactional
    public MarcoDTO criarMarco(MarcoDTO dto, Long criadorId) {
        Usuario criador = usuarioRepository.findById(criadorId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com id: " + criadorId));

        Marco marco = new Marco();
        marco.setNome(dto.getNome());
        marco.setData(dto.getData());
        marco.setCriador(criador);

        Marco salvo = marcoRepository.save(marco);
        log.info("Marco criado: id={}, nome={}", salvo.getId(), dto.getNome());
        return toMarcoDTO(salvo);
    }

    @Transactional
    public void excluirMarco(Long id) {
        marcoRepository.deleteById(id);
    }

    private DependenciaTarefaDTO toDependenciaDTO(DependenciaTarefa d) {
        DependenciaTarefaDTO dto = new DependenciaTarefaDTO();
        dto.setId(d.getId());
        dto.setTarefaId(d.getTarefa().getId());
        dto.setTarefaTitulo(d.getTarefa().getTitulo());
        dto.setTarefaDependenteId(d.getTarefaDependente().getId());
        dto.setTarefaDependenteTitulo(d.getTarefaDependente().getTitulo());
        dto.setTipo(d.getTipo().name());
        return dto;
    }

    private MarcoDTO toMarcoDTO(Marco m) {
        MarcoDTO dto = new MarcoDTO();
        dto.setId(m.getId());
        dto.setNome(m.getNome());
        dto.setData(m.getData());
        if (m.getCriador() != null) {
            dto.setCriadorId(m.getCriador().getId());
            dto.setCriadorNome(m.getCriador().getNome());
        }
        return dto;
    }
}
