package com.taskflow.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "DEPENDENCIA_TAREFA")
public class DependenciaTarefa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarefa_id", nullable = false)
    private Tarefa tarefa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarefa_dependente_id", nullable = false)
    private Tarefa tarefaDependente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoDependencia tipo;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @PrePersist
    protected void onCreate() {
        this.dataCriacao = LocalDateTime.now();
    }

    public DependenciaTarefa() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Tarefa getTarefa() { return tarefa; }
    public void setTarefa(Tarefa tarefa) { this.tarefa = tarefa; }
    public Tarefa getTarefaDependente() { return tarefaDependente; }
    public void setTarefaDependente(Tarefa tarefaDependente) { this.tarefaDependente = tarefaDependente; }
    public TipoDependencia getTipo() { return tipo; }
    public void setTipo(TipoDependencia tipo) { this.tipo = tipo; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
}
