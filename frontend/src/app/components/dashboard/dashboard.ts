import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { DashboardService, Dashboard as DashboardData, TarefasPorStatus, TarefasPorCategoria, TarefasPorResponsavel, Tendencia } from '../../services/dashboard.service';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class DashboardPage implements OnInit {
  data: DashboardData | null = null;
  loading = true;

  statusColors: Record<string, string> = {
    PENDENTE: '#f59e0b',
    EM_EXECUCAO: '#3b82f6',
    CONCLUIDA: '#22c55e',
    ARQUIVADA: '#9ca3af'
  };

  statusLabels: Record<string, string> = {
    PENDENTE: 'Pendente',
    EM_EXECUCAO: 'Em Execução',
    CONCLUIDA: 'Concluída',
    ARQUIVADA: 'Arquivada'
  };

  constructor(private dashboardService: DashboardService) {}

  ngOnInit() {
    this.dashboardService.buscar().subscribe({
      next: (data) => { this.data = data; this.loading = false; },
      error: () => this.loading = false
    });
  }

  get maxStatus(): number {
    if (!this.data?.porStatus) return 1;
    return Math.max(...this.data.porStatus.map(s => s.contagem), 1);
  }

  get maxCategoria(): number {
    if (!this.data?.porCategoria) return 1;
    return Math.max(...this.data.porCategoria.map(c => c.contagem), 1);
  }

  get maxTendencia(): number {
    if (!this.data?.tendencia) return 1;
    return Math.max(...this.data.tendencia.map(t => t.criadas + t.concluidas), 1);
  }

  barWidth(valor: number, max: number): string {
    return (valor / max * 100) + '%';
  }

  tendenciaBarHeight(valor: number): string {
    return (valor / this.maxTendencia * 100) + '%';
  }
}
