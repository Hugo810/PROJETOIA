import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Historico } from '../models/collaboration';

@Injectable({ providedIn: 'root' })
export class HistoricoService {
  private baseUrl = '/api';

  constructor(private http: HttpClient) {}

  listarPorTarefa(tarefaId: number): Observable<Historico[]> {
    return this.http.get<Historico[]>(`${this.baseUrl}/tarefas/${tarefaId}/historico`);
  }
}
