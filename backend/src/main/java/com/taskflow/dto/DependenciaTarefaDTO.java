package com.taskflow.dto;

public class DependenciaTarefaDTO {
    private Long id;
    private Long tarefaId;
    private String tarefaTitulo;
    private Long tarefaDependenteId;
    private String tarefaDependenteTitulo;
    private String tipo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTarefaId() { return tarefaId; }
    public void setTarefaId(Long tarefaId) { this.tarefaId = tarefaId; }
    public String getTarefaTitulo() { return tarefaTitulo; }
    public void setTarefaTitulo(String tarefaTitulo) { this.tarefaTitulo = tarefaTitulo; }
    public Long getTarefaDependenteId() { return tarefaDependenteId; }
    public void setTarefaDependenteId(Long tarefaDependenteId) { this.tarefaDependenteId = tarefaDependenteId; }
    public String getTarefaDependenteTitulo() { return tarefaDependenteTitulo; }
    public void setTarefaDependenteTitulo(String tarefaDependenteTitulo) { this.tarefaDependenteTitulo = tarefaDependenteTitulo; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}
