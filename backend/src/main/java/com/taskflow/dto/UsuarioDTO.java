package com.taskflow.dto;

import com.taskflow.model.UsuarioRole;

public class UsuarioDTO {

    private Long id;
    private String nome;
    private String email;
    private UsuarioRole role;

    public UsuarioDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public UsuarioRole getRole() { return role; }
    public void setRole(UsuarioRole role) { this.role = role; }
}
