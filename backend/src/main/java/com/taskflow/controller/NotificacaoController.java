package com.taskflow.controller;

import com.taskflow.dto.NotificacaoDTO;
import com.taskflow.service.NotificacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificacoes")
@CrossOrigin(origins = "*")
public class NotificacaoController {

    private final NotificacaoService service;

    public NotificacaoController(NotificacaoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<NotificacaoDTO>> listar(@RequestParam Long usuarioId) {
        return ResponseEntity.ok(service.listarPorUsuario(usuarioId));
    }

    @GetMapping("/nao-lidas")
    public ResponseEntity<List<NotificacaoDTO>> naoLidas(@RequestParam Long usuarioId) {
        return ResponseEntity.ok(service.listarNaoLidas(usuarioId));
    }

    @GetMapping("/contagem")
    public ResponseEntity<Map<String, Long>> contagem(@RequestParam Long usuarioId) {
        return ResponseEntity.ok(Map.of("naoLidas", service.countNaoLidas(usuarioId)));
    }

    @PatchMapping("/{id}/lida")
    public ResponseEntity<Void> marcarComoLida(@PathVariable Long id) {
        service.marcarComoLida(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/lidas")
    public ResponseEntity<Void> marcarTodasComoLidas(@RequestParam Long usuarioId) {
        service.marcarTodasComoLidas(usuarioId);
        return ResponseEntity.ok().build();
    }
}
