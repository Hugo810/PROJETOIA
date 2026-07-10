package com.taskflow.repository;

import com.taskflow.model.Marco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarcoRepository extends JpaRepository<Marco, Long> {
    List<Marco> findAllByOrderByDataAsc();
    List<Marco> findByDataBetweenOrderByDataAsc(java.time.LocalDateTime inicio, java.time.LocalDateTime fim);
}
