package com.taskflow.dto;

import java.time.LocalDateTime;

public class HistoricoDTO {

    private Long id;
    private Long tarefaId;
    private String tarefaTitulo;
    private Long usuarioId;
    private String usuarioNome;
    private String campo;
    private String valorAnterior;
    private String valorNovo;
    private LocalDateTime dataAlteracao;

    public HistoricoDTO() {}

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
    public String getCampo() { return campo; }
    public void setCampo(String campo) { this.campo = campo; }
    public String getValorAnterior() { return valorAnterior; }
    public void setValorAnterior(String valorAnterior) { this.valorAnterior = valorAnterior; }
    public String getValorNovo() { return valorNovo; }
    public void setValorNovo(String valorNovo) { this.valorNovo = valorNovo; }
    public LocalDateTime getDataAlteracao() { return dataAlteracao; }
    public void setDataAlteracao(LocalDateTime dataAlteracao) { this.dataAlteracao = dataAlteracao; }
}
