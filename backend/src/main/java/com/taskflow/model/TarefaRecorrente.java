package com.taskflow.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "TAREFA_RECORRENTE")
public class TarefaRecorrente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarefa_modelo_id", nullable = false)
    private Tarefa tarefaModelo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Recorrencia recorrencia;

    @Column(length = 500)
    private String configuracao;

    @Column(name = "proxima_execucao", nullable = false)
    private LocalDate proximaExecucao;

    @Column(nullable = false)
    private Boolean ativa = true;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criador_id")
    private Usuario criador;

    @PrePersist
    protected void onCreate() {
        this.dataCriacao = LocalDateTime.now();
    }

    public TarefaRecorrente() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Tarefa getTarefaModelo() { return tarefaModelo; }
    public void setTarefaModelo(Tarefa tarefaModelo) { this.tarefaModelo = tarefaModelo; }
    public Recorrencia getRecorrencia() { return recorrencia; }
    public void setRecorrencia(Recorrencia recorrencia) { this.recorrencia = recorrencia; }
    public String getConfiguracao() { return configuracao; }
    public void setConfiguracao(String configuracao) { this.configuracao = configuracao; }
    public LocalDate getProximaExecucao() { return proximaExecucao; }
    public void setProximaExecucao(LocalDate proximaExecucao) { this.proximaExecucao = proximaExecucao; }
    public Boolean getAtiva() { return ativa; }
    public void setAtiva(Boolean ativa) { this.ativa = ativa; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public Usuario getCriador() { return criador; }
    public void setCriador(Usuario criador) { this.criador = criador; }
}
