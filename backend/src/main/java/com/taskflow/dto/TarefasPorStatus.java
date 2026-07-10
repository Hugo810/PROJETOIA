package com.taskflow.dto;

public class TarefasPorStatus {
    private String status;
    private long contagem;

    public TarefasPorStatus() {}

    public TarefasPorStatus(String status, long contagem) {
        this.status = status;
        this.contagem = contagem;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getContagem() { return contagem; }
    public void setContagem(long contagem) { this.contagem = contagem; }
}
