package com.taskflow.dto;

import java.time.LocalDateTime;

public class NotificacaoDTO {

    private Long id;
    private String mensagem;
    private Long usuarioId;
    private Long tarefaId;
    private String tarefaTitulo;
    private Boolean lida;
    private LocalDateTime dataCriacao;
    private String tipo;

    public NotificacaoDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public Long getTarefaId() { return tarefaId; }
    public void setTarefaId(Long tarefaId) { this.tarefaId = tarefaId; }
    public String getTarefaTitulo() { return tarefaTitulo; }
    public void setTarefaTitulo(String tarefaTitulo) { this.tarefaTitulo = tarefaTitulo; }
    public Boolean getLida() { return lida; }
    public void setLida(Boolean lida) { this.lida = lida; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}
