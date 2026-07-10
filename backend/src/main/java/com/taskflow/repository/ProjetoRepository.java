package com.taskflow.repository;

import com.taskflow.model.Projeto;
import com.taskflow.model.ProjetoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjetoRepository extends JpaRepository<Projeto, Long> {
    List<Projeto> findByStatus(ProjetoStatus status);
    List<Projeto> findByResponsavelId(Long responsavelId);
}
