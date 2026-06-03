package com.taskflow.service;

import com.taskflow.dto.TarefaDTO;
import com.taskflow.model.Categoria;
import com.taskflow.model.Tarefa;
import com.taskflow.model.TarefaStatus;
import com.taskflow.model.Usuario;
import com.taskflow.repository.TarefaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TarefaService {

    private static final Logger log = LoggerFactory.getLogger(TarefaService.class);

    private final TarefaRepository tarefaRepository;
    private final CategoriaService categoriaService;
    private final UsuarioService usuarioService;

    public TarefaService(TarefaRepository tarefaRepository, CategoriaService categoriaService, UsuarioService usuarioService) {
        this.tarefaRepository = tarefaRepository;
        this.categoriaService = categoriaService;
        this.usuarioService = usuarioService;
    }

    public Page<TarefaDTO> listar(String status, Long categoriaId, Long responsavelId, int pagina, int tamanho) {
        PageRequest pageRequest = PageRequest.of(pagina, tamanho);
        TarefaStatus statusEnum = status != null ? TarefaStatus.valueOf(status) : null;
        Page<Tarefa> page;

        if (statusEnum != null && categoriaId != null && responsavelId != null) {
            page = tarefaRepository.findByStatusAndCategoriaIdAndResponsavelIdOrderByPrazoAsc(statusEnum, categoriaId, responsavelId, pageRequest);
        } else if (statusEnum != null && categoriaId != null) {
            page = tarefaRepository.findByStatusAndCategoriaIdOrderByPrazoAsc(statusEnum, categoriaId, pageRequest);
        } else if (statusEnum != null && responsavelId != null) {
            page = tarefaRepository.findByStatusAndResponsavelIdOrderByPrazoAsc(statusEnum, responsavelId, pageRequest);
        } else if (categoriaId != null && responsavelId != null) {
            page = tarefaRepository.findByCategoriaIdAndResponsavelIdOrderByPrazoAsc(categoriaId, responsavelId, pageRequest);
        } else if (statusEnum != null) {
            page = tarefaRepository.findByStatusOrderByPrazoAsc(statusEnum, pageRequest);
        } else if (categoriaId != null) {
            page = tarefaRepository.findByCategoriaIdOrderByPrazoAsc(categoriaId, pageRequest);
        } else if (responsavelId != null) {
            page = tarefaRepository.findByResponsavelIdOrderByPrazoAsc(responsavelId, pageRequest);
        } else {
            page = tarefaRepository.findAllByOrderByPrazoAsc(pageRequest);
        }

        return page.map(this::toDTO);
    }

    public TarefaDTO buscarPorId(Long id) {
        return tarefaRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Tarefa nao encontrada: " + id));
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
        return saved;
    }

    public TarefaDTO distribuir(Long id, Long responsavelId, Long distribuidorId) {
        usuarioService.validarRole(distribuidorId, com.taskflow.model.UsuarioRole.DISTRIBUIDOR);
        Tarefa tarefa = tarefaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarefa nao encontrada: " + id));
        Usuario responsavel = usuarioService.findEntityById(responsavelId);
        tarefa.setResponsavel(responsavel);
        tarefa.setDistribuidorId(distribuidorId);
        TarefaDTO saved = toDTO(tarefaRepository.save(tarefa));
        log.info("Usuario {} distribuiu tarefa {} para usuario {}", distribuidorId, id, responsavelId);
        return saved;
    }

    public TarefaDTO atualizar(Long id, TarefaDTO dto, Long usuarioId) {
        usuarioService.validarRole(usuarioId, com.taskflow.model.UsuarioRole.DISTRIBUIDOR);
        Tarefa tarefa = tarefaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarefa nao encontrada: " + id));

        tarefa.setTitulo(dto.getTitulo());
        tarefa.setDescricao(dto.getDescricao());
        tarefa.setPrazo(dto.getPrazo());

        if (dto.getCategoriaId() != null && !dto.getCategoriaId().equals(tarefa.getCategoria().getId())) {
            Categoria categoria = categoriaService.findEntityById(dto.getCategoriaId());
            tarefa.setCategoria(categoria);
        }

        TarefaDTO saved = toDTO(tarefaRepository.save(tarefa));
        log.info("Usuario {} atualizou tarefa {}", usuarioId, id);
        return saved;
    }

    public TarefaDTO iniciar(Long id, Long usuarioId) {
        Tarefa tarefa = tarefaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarefa nao encontrada: " + id));
        if (tarefa.getResponsavel() != null && !tarefa.getResponsavel().getId().equals(usuarioId)) {
            usuarioService.validarRole(usuarioId, com.taskflow.model.UsuarioRole.DISTRIBUIDOR);
        }
        tarefa.setStatus(TarefaStatus.EM_EXECUCAO);
        TarefaDTO saved = toDTO(tarefaRepository.save(tarefa));
        log.info("Usuario {} iniciou tarefa {}", usuarioId, id);
        return saved;
    }

    public TarefaDTO concluir(Long id, Long usuarioId) {
        Tarefa tarefa = tarefaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarefa nao encontrada: " + id));
        if (tarefa.getResponsavel() != null && !tarefa.getResponsavel().getId().equals(usuarioId)) {
            usuarioService.validarRole(usuarioId, com.taskflow.model.UsuarioRole.DISTRIBUIDOR);
        }
        tarefa.setStatus(TarefaStatus.CONCLUIDA);
        tarefa.setDataConclusao(LocalDateTime.now());
        TarefaDTO saved = toDTO(tarefaRepository.save(tarefa));
        log.info("Usuario {} concluiu tarefa {}", usuarioId, id);
        return saved;
    }

    public void excluir(Long id, Long usuarioId) {
        usuarioService.validarRole(usuarioId, com.taskflow.model.UsuarioRole.ADMIN);
        if (!tarefaRepository.existsById(id)) {
            throw new RuntimeException("Tarefa nao encontrada: " + id);
        }
        tarefaRepository.deleteById(id);
        log.info("Usuario {} excluiu tarefa {}", usuarioId, id);
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
