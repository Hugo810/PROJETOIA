import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RegistroTempo } from '../models/tempo';

@Injectable({ providedIn: 'root' })
export class TempoService {
  private baseUrl = '/api';

  constructor(private http: HttpClient) {}

  listarPorTarefa(tarefaId: number): Observable<{ registros: RegistroTempo[]; totalMinutos: number }> {
    return this.http.get<{ registros: RegistroTempo[]; totalMinutos: number }>(`${this.baseUrl}/tarefas/${tarefaId}/tempo`);
  }

  iniciarTimer(tarefaId: number): Observable<RegistroTempo> {
    return this.http.post<RegistroTempo>(`${this.baseUrl}/tarefas/${tarefaId}/tempo`, {});
  }

  pararTimer(registroId: number): Observable<RegistroTempo> {
    return this.http.patch<RegistroTempo>(`${this.baseUrl}/registros-tempo/${registroId}/parar`, {});
  }

  pararTimerPorTarefa(tarefaId: number): Observable<RegistroTempo> {
    return this.http.patch<RegistroTempo>(`${this.baseUrl}/tarefas/${tarefaId}/tempo/parar`, {});
  }

  registrarManual(tarefaId: number, duracaoMinutos: number, descricao?: string): Observable<RegistroTempo> {
    return this.http.post<RegistroTempo>(`${this.baseUrl}/tarefas/${tarefaId}/tempo/manual`, { duracaoMinutos, descricao });
  }

  listarPorUsuarioPeriodo(usuarioId: number, inicio: string, fim: string): Observable<{ registros: RegistroTempo[]; totalMinutos: number }> {
    return this.http.get<{ registros: RegistroTempo[]; totalMinutos: number }>(
      `${this.baseUrl}/usuarios/${usuarioId}/tempo?inicio=${inicio}&fim=${fim}`
    );
  }

  excluir(registroId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/registros-tempo/${registroId}`);
  }
}
