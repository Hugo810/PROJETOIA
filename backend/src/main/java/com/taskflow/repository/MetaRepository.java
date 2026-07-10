package com.taskflow.repository;

import com.taskflow.model.Meta;
import com.taskflow.model.MetaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetaRepository extends JpaRepository<Meta, Long> {
    List<Meta> findByProjetoIdOrderByDataInicioAsc(Long projetoId);
    List<Meta> findByProjetoIdAndStatus(Long projetoId, MetaStatus status);
}
