package com.taskflow.service;

import com.taskflow.dto.NotificacaoDTO;
import com.taskflow.model.Notificacao;
import com.taskflow.model.Tarefa;
import com.taskflow.model.Usuario;
import com.taskflow.repository.NotificacaoRepository;
import com.taskflow.repository.TarefaRepository;
import com.taskflow.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificacaoService {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoService.class);

    private final NotificacaoRepository notificacaoRepository;
    private final TarefaRepository tarefaRepository;
    private final UsuarioRepository usuarioRepository;

    public NotificacaoService(NotificacaoRepository notificacaoRepository, TarefaRepository tarefaRepository,
                              UsuarioRepository usuarioRepository) {
        this.notificacaoRepository = notificacaoRepository;
        this.tarefaRepository = tarefaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<NotificacaoDTO> listarPorUsuario(Long usuarioId) {
        return notificacaoRepository.findByUsuarioIdOrderByDataCriacaoDesc(usuarioId)
                .stream().map(this::toDTO).toList();
    }

    public List<NotificacaoDTO> listarNaoLidas(Long usuarioId) {
        return notificacaoRepository.findByUsuarioIdAndLidaFalseOrderByDataCriacaoDesc(usuarioId)
                .stream().map(this::toDTO).toList();
    }

    public long countNaoLidas(Long usuarioId) {
        return notificacaoRepository.countByUsuarioIdAndLidaFalse(usuarioId);
    }

    public void marcarComoLida(Long id) {
        notificacaoRepository.findById(id).ifPresent(n -> {
            n.setLida(true);
            notificacaoRepository.save(n);
        });
    }

    public void marcarTodasComoLidas(Long usuarioId) {
        notificacaoRepository.marcarTodasComoLidas(usuarioId);
        log.info("Todas notificacoes marcadas como lidas para usuario {}", usuarioId);
    }

    public void criar(String mensagem, Long usuarioId, Long tarefaId, String tipo) {
        if (usuarioId == null) return;
        Notificacao n = new Notificacao();
        n.setMensagem(mensagem);
        n.setUsuario(usuarioRepository.getReferenceById(usuarioId));
        n.setTipo(tipo);
        if (tarefaId != null) {
            n.setTarefa(tarefaRepository.getReferenceById(tarefaId));
        }
        notificacaoRepository.save(n);
        log.info("Notificacao criada para usuario {}: {}", usuarioId, mensagem);
    }

    private NotificacaoDTO toDTO(Notificacao n) {
        NotificacaoDTO dto = new NotificacaoDTO();
        dto.setId(n.getId());
        dto.setMensagem(n.getMensagem());
        dto.setUsuarioId(n.getUsuario().getId());
        dto.setLida(n.getLida());
        dto.setDataCriacao(n.getDataCriacao());
        dto.setTipo(n.getTipo());
        if (n.getTarefa() != null) {
            dto.setTarefaId(n.getTarefa().getId());
            dto.setTarefaTitulo(n.getTarefa().getTitulo());
        }
        return dto;
    }
}
