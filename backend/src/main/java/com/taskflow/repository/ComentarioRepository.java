package com.taskflow.repository;

import com.taskflow.model.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
    List<Comentario> findByTarefaIdOrderByDataCriacaoAsc(Long tarefaId);
    long countByTarefaId(Long tarefaId);
}
