package com.taskflow.dto;

public class Tendencia {
    private String data;
    private long criadas;
    private long concluidas;

    public Tendencia() {}

    public Tendencia(String data, long criadas, long concluidas) {
        this.data = data;
        this.criadas = criadas;
        this.concluidas = concluidas;
    }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    public long getCriadas() { return criadas; }
    public void setCriadas(long criadas) { this.criadas = criadas; }
    public long getConcluidas() { return concluidas; }
    public void setConcluidas(long concluidas) { this.concluidas = concluidas; }
}
