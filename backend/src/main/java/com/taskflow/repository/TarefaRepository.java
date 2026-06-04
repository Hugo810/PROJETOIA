package com.taskflow.repository;

import com.taskflow.model.Tarefa;
import com.taskflow.model.TarefaStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    Page<Tarefa> findByStatusOrderByPrazoAsc(TarefaStatus status, Pageable pageable);

    Page<Tarefa> findByCategoriaIdOrderByPrazoAsc(Long categoriaId, Pageable pageable);

    Page<Tarefa> findByStatusAndCategoriaIdOrderByPrazoAsc(TarefaStatus status, Long categoriaId, Pageable pageable);

    Page<Tarefa> findByResponsavelIdOrderByPrazoAsc(Long responsavelId, Pageable pageable);

    Page<Tarefa> findByStatusAndResponsavelIdOrderByPrazoAsc(TarefaStatus status, Long responsavelId, Pageable pageable);

    Page<Tarefa> findByCategoriaIdAndResponsavelIdOrderByPrazoAsc(Long categoriaId, Long responsavelId, Pageable pageable);

    Page<Tarefa> findByStatusAndCategoriaIdAndResponsavelIdOrderByPrazoAsc(TarefaStatus status, Long categoriaId, Long responsavelId, Pageable pageable);

    Page<Tarefa> findAllByOrderByPrazoAsc(Pageable pageable);
}
