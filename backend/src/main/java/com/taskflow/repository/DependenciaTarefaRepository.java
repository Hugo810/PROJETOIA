package com.taskflow.repository;

import com.taskflow.model.DependenciaTarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DependenciaTarefaRepository extends JpaRepository<DependenciaTarefa, Long> {
    List<DependenciaTarefa> findByTarefaId(Long tarefaId);
    List<DependenciaTarefa> findByTarefaDependenteId(Long tarefaId);
    boolean existsByTarefaIdAndTarefaDependenteId(Long tarefaId, Long tarefaDependenteId);
}
