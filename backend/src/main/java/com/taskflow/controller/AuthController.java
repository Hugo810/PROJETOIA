package com.taskflow.controller;

import com.taskflow.dto.LoginRequest;
import com.taskflow.dto.UsuarioDTO;
import com.taskflow.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UsuarioService service;

    public AuthController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioDTO> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(service.login(req));
    }
}
