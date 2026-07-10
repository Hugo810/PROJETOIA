package com.taskflow.repository;

import com.taskflow.model.Tarefa;
import com.taskflow.model.TarefaStatus;
import com.taskflow.model.Prioridade;
import org.springframework.data.jpa.domain.Specification;

public class TarefaSpecs {

    public static Specification<Tarefa> comStatus(TarefaStatus status) {
        return (root, query, cb) -> status != null ? cb.equal(root.get("status"), status) : null;
    }

    public static Specification<Tarefa> comCategoriaId(Long categoriaId) {
        return (root, query, cb) -> categoriaId != null ? cb.equal(root.get("categoria").get("id"), categoriaId) : null;
    }

    public static Specification<Tarefa> comResponsavelId(Long responsavelId) {
        return (root, query, cb) -> responsavelId != null ? cb.equal(root.get("responsavel").get("id"), responsavelId) : null;
    }

    public static Specification<Tarefa> comPrioridade(Prioridade prioridade) {
        return (root, query, cb) -> prioridade != null ? cb.equal(root.get("prioridade"), prioridade) : null;
    }

    public static Specification<Tarefa> comBusca(String busca) {
        return (root, query, cb) -> {
            if (busca == null || busca.isBlank()) return null;
            String termo = "%" + busca.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("titulo")), termo),
                cb.like(cb.lower(root.get("descricao")), termo)
            );
        };
    }
}
