package com.taskflow.service;

import com.taskflow.config.RegraNegocioException;
import com.taskflow.config.TarefaNotFoundException;
import com.taskflow.dto.RegistroTempoDTO;
import com.taskflow.model.*;
import com.taskflow.repository.RegistroTempoRepository;
import com.taskflow.repository.TarefaRepository;
import com.taskflow.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RegistroTempoService {

    private static final Logger log = LoggerFactory.getLogger(RegistroTempoService.class);

    private final RegistroTempoRepository repository;
    private final TarefaRepository tarefaRepository;
    private final UsuarioRepository usuarioRepository;

    public RegistroTempoService(RegistroTempoRepository repository, TarefaRepository tarefaRepository,
                                UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.tarefaRepository = tarefaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<RegistroTempoDTO> listarPorTarefa(Long tarefaId) {
        return repository.findByTarefaIdOrderByInicioDesc(tarefaId).stream().map(this::toDTO).toList();
    }

    public RegistroTempoDTO iniciarTimer(Long tarefaId, Long usuarioId) {
        Tarefa tarefa = tarefaRepository.findById(tarefaId)
                .orElseThrow(() -> new TarefaNotFoundException(tarefaId));

        repository.findByTarefaIdAndEmAndamentoTrue(tarefaId).ifPresent(r -> {
            throw new RegraNegocioException("Já existe um timer em andamento para esta tarefa");
        });

        repository.findByUsuarioIdAndEmAndamentoTrue(usuarioId).ifPresent(r -> {
            throw new RegraNegocioException("Você já tem um timer em andamento. Pare o timer atual antes de iniciar outro.");
        });

        Usuario usuario = usuarioRepository.getReferenceById(usuarioId);

        RegistroTempo registro = new RegistroTempo();
        registro.setTarefa(tarefa);
        registro.setUsuario(usuario);
        registro.setInicio(LocalDateTime.now());
        registro.setEmAndamento(true);
        registro.setManual(false);

        RegistroTempo saved = repository.save(registro);
        log.info("Timer iniciado para tarefa {} por usuario {}", tarefaId, usuarioId);
        return toDTO(saved);
    }

    public RegistroTempoDTO pararTimer(Long registroId, Long usuarioId) {
        RegistroTempo registro = repository.findById(registroId)
                .orElseThrow(() -> new RegraNegocioException("Registro de tempo não encontrado"));

        if (!registro.getUsuario().getId().equals(usuarioId)) {
            throw new RegraNegocioException("Apenas o usuário que iniciou pode parar o timer");
        }

        if (!registro.getEmAndamento()) {
            throw new RegraNegocioException("Este timer já foi parado");
        }

        registro.setFim(LocalDateTime.now());
        registro.setEmAndamento(false);
        registro.setDuracaoMinutos(Duration.between(registro.getInicio(), registro.getFim()).toMinutes());

        RegistroTempo saved = repository.save(registro);
        log.info("Timer parado para tarefa {} por usuario {}: {} min", registro.getTarefa().getId(), usuarioId, saved.getDuracaoMinutos());
        return toDTO(saved);
    }

    public RegistroTempoDTO pararTimerPorTarefa(Long tarefaId, Long usuarioId) {
        RegistroTempo registro = repository.findByTarefaIdAndEmAndamentoTrue(tarefaId)
                .orElseThrow(() -> new RegraNegocioException("Nenhum timer em andamento para esta tarefa"));
        return pararTimer(registro.getId(), usuarioId);
    }

    public RegistroTempoDTO registrarManual(Long tarefaId, Long usuarioId, Long duracaoMinutos, String descricao) {
        Tarefa tarefa = tarefaRepository.findById(tarefaId)
                .orElseThrow(() -> new TarefaNotFoundException(tarefaId));

        if (duracaoMinutos == null || duracaoMinutos <= 0) {
            throw new RegraNegocioException("Duração deve ser maior que zero");
        }

        Usuario usuario = usuarioRepository.getReferenceById(usuarioId);

        RegistroTempo registro = new RegistroTempo();
        registro.setTarefa(tarefa);
        registro.setUsuario(usuario);
        registro.setInicio(LocalDateTime.now());
        registro.setFim(LocalDateTime.now());
        registro.setDuracaoMinutos(duracaoMinutos);
        registro.setDescricao(descricao);
        registro.setManual(true);
        registro.setEmAndamento(false);

        RegistroTempo saved = repository.save(registro);
        log.info("Registro manual de {} min criado para tarefa {} por usuario {}", duracaoMinutos, tarefaId, usuarioId);
        return toDTO(saved);
    }

    public long totalMinutosTarefa(Long tarefaId) {
        return repository.sumDuracaoByTarefaId(tarefaId);
    }

    public long totalMinutosUsuarioPeriodo(Long usuarioId, LocalDateTime inicio, LocalDateTime fim) {
        return repository.sumDuracaoByUsuarioAndPeriodo(usuarioId, inicio, fim);
    }

    public List<RegistroTempoDTO> listarPorUsuarioPeriodo(Long usuarioId, LocalDateTime inicio, LocalDateTime fim) {
        return repository.findByUsuarioAndPeriodo(usuarioId, inicio, fim).stream().map(this::toDTO).toList();
    }

    public RegistroTempoDTO buscarPorId(Long id) {
        return repository.findById(id).map(this::toDTO)
                .orElseThrow(() -> new RegraNegocioException("Registro de tempo não encontrado"));
    }

    public void excluir(Long id, Long usuarioId) {
        RegistroTempo registro = repository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Registro de tempo não encontrado"));
        if (!registro.getUsuario().getId().equals(usuarioId)) {
            throw new RegraNegocioException("Apenas o criador pode excluir o registro");
        }
        if (registro.getEmAndamento()) {
            throw new RegraNegocioException("Não é possível excluir um timer em andamento. Pare-o primeiro.");
        }
        repository.deleteById(id);
        log.info("Registro de tempo {} excluido por usuario {}", id, usuarioId);
    }

    private RegistroTempoDTO toDTO(RegistroTempo r) {
        RegistroTempoDTO dto = new RegistroTempoDTO();
        dto.setId(r.getId());
        dto.setTarefaId(r.getTarefa().getId());
        dto.setTarefaTitulo(r.getTarefa().getTitulo());
        dto.setUsuarioId(r.getUsuario().getId());
        dto.setUsuarioNome(r.getUsuario().getNome());
        dto.setInicio(r.getInicio());
        dto.setFim(r.getFim());
        dto.setDuracaoMinutos(r.getDuracaoMinutos());
        dto.setDescricao(r.getDescricao());
        dto.setManual(r.getManual());
        dto.setEmAndamento(r.getEmAndamento());
        return dto;
    }
}
