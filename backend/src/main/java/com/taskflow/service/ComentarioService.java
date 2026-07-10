package com.taskflow.service;

import com.taskflow.config.RegraNegocioException;
import com.taskflow.config.TarefaNotFoundException;
import com.taskflow.config.UsuarioNotFoundException;
import com.taskflow.dto.ComentarioDTO;
import com.taskflow.model.Comentario;
import com.taskflow.model.Tarefa;
import com.taskflow.model.Usuario;
import com.taskflow.repository.ComentarioRepository;
import com.taskflow.repository.TarefaRepository;
import com.taskflow.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ComentarioService {

    private static final Logger log = LoggerFactory.getLogger(ComentarioService.class);

    private final ComentarioRepository comentarioRepository;
    private final TarefaRepository tarefaRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacaoService notificacaoService;

    public ComentarioService(ComentarioRepository comentarioRepository, TarefaRepository tarefaRepository,
                             UsuarioRepository usuarioRepository, NotificacaoService notificacaoService) {
        this.comentarioRepository = comentarioRepository;
        this.tarefaRepository = tarefaRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacaoService = notificacaoService;
    }

    public List<ComentarioDTO> listarPorTarefa(Long tarefaId) {
        return comentarioRepository.findByTarefaIdOrderByDataCriacaoAsc(tarefaId)
                .stream().map(this::toDTO).toList();
    }

    public ComentarioDTO criar(ComentarioDTO dto, Long autorId) {
        Tarefa tarefa = tarefaRepository.findById(dto.getTarefaId())
                .orElseThrow(() -> new TarefaNotFoundException(dto.getTarefaId()));
        Usuario autor = usuarioRepository.findById(autorId)
                .orElseThrow(() -> new UsuarioNotFoundException(autorId));

        Comentario c = new Comentario();
        c.setTexto(dto.getTexto());
        c.setTarefa(tarefa);
        c.setAutor(autor);

        Comentario saved = comentarioRepository.save(c);
        log.info("Usuario {} comentou na tarefa {}", autorId, dto.getTarefaId());

        notificacaoService.criar(
                "Novo comentário de " + autor.getNome() + " na tarefa \"" + tarefa.getTitulo() + "\"",
                tarefa.getResponsavel() != null ? tarefa.getResponsavel().getId() : null,
                tarefa.getId(),
                "COMENTARIO"
        );

        return toDTO(saved);
    }

    public ComentarioDTO atualizar(Long id, ComentarioDTO dto, Long usuarioId) {
        Comentario c = comentarioRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Comentário não encontrado"));
        if (!c.getAutor().getId().equals(usuarioId)) {
            throw new RegraNegocioException("Apenas o autor pode editar o comentário");
        }
        c.setTexto(dto.getTexto());
        Comentario saved = comentarioRepository.save(c);
        log.info("Usuario {} atualizou comentario {}", usuarioId, id);
        return toDTO(saved);
    }

    public void excluir(Long id, Long usuarioId) {
        Comentario c = comentarioRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Comentário não encontrado"));
        if (!c.getAutor().getId().equals(usuarioId)) {
            throw new RegraNegocioException("Apenas o autor pode excluir o comentário");
        }
        comentarioRepository.deleteById(id);
        log.info("Usuario {} excluiu comentario {}", usuarioId, id);
    }

    private ComentarioDTO toDTO(Comentario c) {
        ComentarioDTO dto = new ComentarioDTO();
        dto.setId(c.getId());
        dto.setTexto(c.getTexto());
        dto.setTarefaId(c.getTarefa().getId());
        dto.setAutorId(c.getAutor().getId());
        dto.setAutorNome(c.getAutor().getNome());
        dto.setDataCriacao(c.getDataCriacao());
        dto.setDataAtualizacao(c.getDataAtualizacao());
        return dto;
    }
}
