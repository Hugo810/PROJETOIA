import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TaskService, ResumoTarefas } from '../../services/task.service';
import { AuthService } from '../../services/auth.service';
import { Tarefa } from '../../models/task';

@Component({
  selector: 'app-daily-tasks',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './daily-tasks.html',
  styleUrl: './daily-tasks.css'
})
export class DailyTasks implements OnInit {
  resumo: ResumoTarefas | null = null;
  loading = true;
  abaAtiva: 'hoje' | 'semana' | 'atrasadas' | 'proximas' = 'hoje';

  diasSemana = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];
  meses = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];

  constructor(
    private taskService: TaskService,
    private auth: AuthService
  ) {}

  ngOnInit() {
    this.carregarResumo();
  }

  carregarResumo() {
    this.loading = true;
    this.taskService.resumo().subscribe({
      next: (data) => {
        this.resumo = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  get tarefasAba(): Tarefa[] {
    if (!this.resumo) return [];
    switch (this.abaAtiva) {
      case 'hoje': return this.resumo.hoje;
      case 'semana': return this.resumo.semana;
      case 'atrasadas': return this.resumo.atrasadas;
      case 'proximas': return this.resumo.proximas;
    }
  }

  concluir(tarefa: Tarefa) {
    if (!tarefa.id) return;
    this.taskService.concluirTarefa(tarefa.id).subscribe({
      next: () => this.carregarResumo()
    });
  }

  podeConcluir(tarefa: Tarefa): boolean {
    return tarefa.status === 'EM_EXECUCAO' || tarefa.status === 'PENDENTE';
  }

  podeIniciar(tarefa: Tarefa): boolean {
    return tarefa.status === 'PENDENTE';
  }

  iniciar(tarefa: Tarefa) {
    if (!tarefa.id) return;
    this.taskService.iniciarTarefa(tarefa.id).subscribe({
      next: () => this.carregarResumo()
    });
  }

  getPrioridadeClass(prioridade?: string): string {
    if (prioridade === 'ALTA') return 'prioridade-alta';
    if (prioridade === 'BAIXA') return 'prioridade-baixa';
    return 'prioridade-media';
  }

  getStatusClass(status: string): string {
    if (status === 'CONCLUIDA') return 'status-concluida';
    if (status === 'EM_EXECUCAO') return 'status-execucao';
    return 'status-pendente';
  }

  getDiasPrazo(tarefa: Tarefa): number | null {
    if (!tarefa.prazo) return null;
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    const prazo = new Date(tarefa.prazo + 'T00:00:00');
    const diff = prazo.getTime() - hoje.getTime();
    return Math.ceil(diff / (1000 * 60 * 60 * 24));
  }

  getBadgeColor(nome: string): string {
    const colors = ['#4361ee', '#e63946', '#2a9d8f', '#e9c46a', '#f4a261', '#264653'];
    let hash = 0;
    for (let i = 0; i < nome.length; i++) {
      hash = nome.charCodeAt(i) + ((hash << 5) - hash);
    }
    return colors[Math.abs(hash) % colors.length];
  }

  formatarData(data: string): string {
    const d = new Date(data + 'T00:00:00');
    return `${d.getDate()} ${this.meses[d.getMonth()]}`;
  }
}
