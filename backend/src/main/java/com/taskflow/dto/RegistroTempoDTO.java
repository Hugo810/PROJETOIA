package com.taskflow.dto;

import java.time.LocalDateTime;

public class RegistroTempoDTO {

    private Long id;
    private Long tarefaId;
    private String tarefaTitulo;
    private Long usuarioId;
    private String usuarioNome;
    private LocalDateTime inicio;
    private LocalDateTime fim;
    private Long duracaoMinutos;
    private String descricao;
    private Boolean manual;
    private Boolean emAndamento;

    public RegistroTempoDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTarefaId() { return tarefaId; }
    public void setTarefaId(Long tarefaId) { this.tarefaId = tarefaId; }
    public String getTarefaTitulo() { return tarefaTitulo; }
    public void setTarefaTitulo(String tarefaTitulo) { this.tarefaTitulo = tarefaTitulo; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getUsuarioNome() { return usuarioNome; }
    public void setUsuarioNome(String usuarioNome) { this.usuarioNome = usuarioNome; }
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
