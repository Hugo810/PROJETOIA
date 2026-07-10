import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { GanttData, Dependencia, Marco } from '../models/gantt';

export type { GanttData, GanttTarefa, Dependencia, Marco } from '../models/gantt';

@Injectable({ providedIn: 'root' })
export class GanttService {
  private baseUrl = '/api';

  constructor(private http: HttpClient) {}

  buscar(inicio?: string, fim?: string): Observable<GanttData> {
    let url = `${this.baseUrl}/gantt`;
    const params: string[] = [];
    if (inicio) params.push(`inicio=${inicio}`);
    if (fim) params.push(`fim=${fim}`);
    if (params.length) url += '?' + params.join('&');
    return this.http.get<GanttData>(url);
  }

  criarDependencia(tarefaId: number, tarefaDependenteId: number, tipo: string): Observable<Dependencia> {
    return this.http.post<Dependencia>(`${this.baseUrl}/tarefas/${tarefaId}/dependencias`, { tarefaDependenteId, tipo });
  }

  excluirDependencia(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/dependencias/${id}`);
  }

  criarMarco(dados: Partial<Marco>): Observable<Marco> {
    return this.http.post<Marco>(`${this.baseUrl}/marcos`, dados);
  }

  excluirMarco(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/marcos/${id}`);
  }
}
