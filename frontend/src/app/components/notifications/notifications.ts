import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificacaoService } from '../../services/notificacao.service';
import { AuthService } from '../../services/auth.service';
import { Notificacao } from '../../models/collaboration';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notifications.html',
  styleUrl: './notifications.css'
})
export class Notifications implements OnInit {
  notificacoes: Notificacao[] = [];
  naoLidas = 0;
  aberto = false;

  meses = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];

  constructor(
    private notificacaoService: NotificacaoService,
    private auth: AuthService
  ) {}

  ngOnInit() {
    this.carregarContagem();
  }

  get usuarioId(): number | null {
    const user = this.auth.getUsuarioLogado();
    return user ? user.id : null;
  }

  carregarContagem() {
    if (!this.usuarioId) return;
    this.notificacaoService.contagem(this.usuarioId).subscribe({
      next: (data) => { this.naoLidas = data.naoLidas; }
    });
  }

  toggle() {
    this.aberto = !this.aberto;
    if (this.aberto && this.usuarioId) {
      this.notificacaoService.listar(this.usuarioId).subscribe({
        next: (data) => { this.notificacoes = data; }
      });
    }
  }

  marcarLida(n: Notificacao) {
    if (n.lida) return;
    this.notificacaoService.marcarComoLida(n.id!).subscribe({
      next: () => {
        n.lida = true;
        this.naoLidas = Math.max(0, this.naoLidas - 1);
      }
    });
  }

  marcarTodasLidas() {
    if (!this.usuarioId) return;
    this.notificacaoService.marcarTodasComoLidas(this.usuarioId).subscribe({
      next: () => {
        this.notificacoes.forEach(n => n.lida = true);
        this.naoLidas = 0;
      }
    });
  }

  fechar() {
    this.aberto = false;
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

  getIcone(tipo: string): string {
    switch (tipo) {
      case 'ATRIBUICAO': return 'M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2';
      case 'COMENTARIO': return 'M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z';
      case 'CONCLUSAO': return 'M20 6L9 17l-5-5';
      default: return 'M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9';
    }
  }
}
