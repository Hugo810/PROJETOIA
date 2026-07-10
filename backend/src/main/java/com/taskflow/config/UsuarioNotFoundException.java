package com.taskflow.config;

public class UsuarioNotFoundException extends RuntimeException {
    public UsuarioNotFoundException(Long id) {
        super("Usuário não encontrado: " + id);
    }

    public UsuarioNotFoundException(String email) {
        super("Usuário não encontrado: " + email);
    }
}
