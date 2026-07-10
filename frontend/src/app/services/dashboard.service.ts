import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface DashboardResumo {
  totalTarefas: number;
  pendentes: number;
  emExecucao: number;
  concluidas: number;
  atrasadas: number;
  criadasUltimos7Dias: number;
  concluidasUltimos7Dias: number;
}

export interface TarefasPorStatus {
  status: string;
  contagem: number;
}

export interface TarefasPorCategoria {
  categoriaId: number;
  categoria: string;
  contagem: number;
}

export interface TarefasPorResponsavel {
  responsavelId: number;
  responsavel: string;
  total: number;
  concluidas: number;
  taxaConclusao: number;
}

export interface Tendencia {
  data: string;
  criadas: number;
  concluidas: number;
}

export interface Dashboard {
  resumo: DashboardResumo;
  porStatus: TarefasPorStatus[];
  porCategoria: TarefasPorCategoria[];
  porResponsavel: TarefasPorResponsavel[];
  tendencia: Tendencia[];
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private baseUrl = '/api/dashboard';

  constructor(private http: HttpClient) {}

  buscar(): Observable<Dashboard> {
    return this.http.get<Dashboard>(this.baseUrl);
  }
}
