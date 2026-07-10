import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HistoricoService } from '../../services/historico.service';
import { Historico } from '../../models/collaboration';

@Component({
  selector: 'app-task-history',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './task-history.html',
  styleUrl: './task-history.css'
})
export class TaskHistory implements OnInit {
  @Input() tarefaId!: number;

  historicos: Historico[] = [];
  loading = true;
  expandido = false;

  meses = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];

  constructor(private historicoService: HistoricoService) {}

  ngOnInit() {
    this.historicoService.listarPorTarefa(this.tarefaId).subscribe({
      next: (data) => { this.historicos = data; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  toggle() {
    this.expandido = !this.expandido;
  }

  getCampoLabel(campo: string): string {
    const labels: Record<string, string> = {
      titulo: 'Título',
      descricao: 'Descrição',
      prazo: 'Prazo',
      prioridade: 'Prioridade',
      categoria: 'Categoria',
      responsavel: 'Responsável',
      status: 'Status'
    };
    return labels[campo] || campo;
  }

  getStatusLabel(valor?: string): string {
    if (!valor) return '-';
    const labels: Record<string, string> = {
      PENDENTE: 'Pendente',
      EM_EXECUCAO: 'Em Execução',
      CONCLUIDA: 'Concluída',
      ARQUIVADA: 'Arquivada'
    };
    return labels[valor] || valor;
  }

  formatarData(data?: string): string {
    if (!data) return '';
    const d = new Date(data);
    return `${d.getDate()} ${this.meses[d.getMonth()]} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`;
  }

  getBadgeColor(nome: string): string {
    const colors = ['#4361ee', '#e63946', '#2a9d8f', '#e9c46a', '#f4a261', '#264653'];
    let hash = 0;
    for (let i = 0; i < nome.length; i++) hash = nome.charCodeAt(i) + ((hash << 5) - hash);
    return colors[Math.abs(hash) % colors.length];
  }
}
