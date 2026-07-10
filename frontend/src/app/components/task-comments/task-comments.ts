import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ComentarioService } from '../../services/comentario.service';
import { AuthService } from '../../services/auth.service';
import { Comentario } from '../../models/collaboration';

@Component({
  selector: 'app-task-comments',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './task-comments.html',
  styleUrl: './task-comments.css'
})
export class TaskComments implements OnInit {
  @Input() tarefaId!: number;

  comentarios: Comentario[] = [];
  novoComentario = '';
  editandoId: number | null = null;
  textoEditando = '';
  loading = true;
  enviando = false;

  meses = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];

  constructor(
    private comentarioService: ComentarioService,
    private auth: AuthService
  ) {}

  ngOnInit() {
    this.carregar();
  }

  get usuarioLogado(): number | null {
    const user = this.auth.getUsuarioLogado();
    return user ? user.id : null;
  }

  carregar() {
    this.comentarioService.listarPorTarefa(this.tarefaId).subscribe({
      next: (data) => { this.comentarios = data; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  enviar() {
    if (!this.novoComentario.trim()) return;
    this.enviando = true;
    this.comentarioService.criar(this.tarefaId, this.novoComentario.trim()).subscribe({
      next: (c) => {
        this.comentarios.push(c);
        this.novoComentario = '';
        this.enviando = false;
      },
      error: () => { this.enviando = false; }
    });
  }

  iniciarEdicao(c: Comentario) {
    this.editandoId = c.id!;
    this.textoEditando = c.texto;
  }

  cancelarEdicao() {
    this.editandoId = null;
    this.textoEditando = '';
  }

  salvarEdicao(c: Comentario) {
    if (!this.textoEditando.trim()) return;
    this.comentarioService.atualizar(c.id!, this.textoEditando.trim()).subscribe({
      next: (atualizado) => {
        const idx = this.comentarios.findIndex(x => x.id === c.id);
        if (idx >= 0) this.comentarios[idx] = atualizado;
        this.cancelarEdicao();
      }
    });
  }

  excluir(c: Comentario) {
    this.comentarioService.excluir(c.id!).subscribe({
      next: () => { this.comentarios = this.comentarios.filter(x => x.id !== c.id); }
    });
  }

  formatarData(data?: string): string {
    if (!data) return '';
    const d = new Date(data);
    const agora = new Date();
    const diffMs = agora.getTime() - d.getTime();
    const diffMin = Math.floor(diffMs / 60000);
    if (diffMin < 1) return 'agora';
    if (diffMin < 60) return `${diffMin}min`;
    const diffH = Math.floor(diffMin / 60);
    if (diffH < 24) return `${diffH}h`;
    const diffD = Math.floor(diffH / 24);
    if (diffD < 7) return `${diffD}d`;
    return `${d.getDate()} ${this.meses[d.getMonth()]}`;
  }

  getBadgeColor(nome: string): string {
    const colors = ['#4361ee', '#e63946', '#2a9d8f', '#e9c46a', '#f4a261', '#264653'];
    let hash = 0;
    for (let i = 0; i < nome.length; i++) hash = nome.charCodeAt(i) + ((hash << 5) - hash);
    return colors[Math.abs(hash) % colors.length];
  }
}
