package com.taskflow.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "REGISTRO_TEMPO")
public class RegistroTempo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarefa_id", nullable = false)
    private Tarefa tarefa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime inicio;

    private LocalDateTime fim;

    @Column(name = "duracao_minutos")
    private Long duracaoMinutos;

    @Column(length = 500)
    private String descricao;

    @Column(nullable = false)
    private Boolean manual = false;

    @Column(nullable = false)
    private Boolean emAndamento = false;

    @PrePersist
    protected void onCreate() {
        if (this.inicio == null) this.inicio = LocalDateTime.now();
    }

    public RegistroTempo() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Tarefa getTarefa() { return tarefa; }
    public void setTarefa(Tarefa tarefa) { this.tarefa = tarefa; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public LocalDateTime getInicio() { return inicio; }
    public void setInicio(LocalDateTime inicio) { this.inicio = inicio; }
    public LocalDateTime getFim() { return fim; }
    public void setFim(LocalDateTime fim) { this.fim = fim; }
    public Long getDuracaoMinutos() { return duracaoMinutos; }
    public void setDuracaoMinutos(Long duracaoMinutos) { this.duracaoMinutos = duracaoMinutos; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public Boolean getManual() { return manual; }
    public void setManual(Boolean manual) { this.manual = manual; }
    public Boolean getEmAndamento() { return emAndamento; }
    public void setEmAndamento(Boolean emAndamento) { this.emAndamento = emAndamento; }
}
