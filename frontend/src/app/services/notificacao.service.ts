import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Notificacao } from '../models/collaboration';

@Injectable({ providedIn: 'root' })
export class NotificacaoService {
  private baseUrl = '/api/notificacoes';

  constructor(private http: HttpClient) {}

  listar(usuarioId: number): Observable<Notificacao[]> {
    return this.http.get<Notificacao[]>(`${this.baseUrl}?usuarioId=${usuarioId}`);
  }

  naoLidas(usuarioId: number): Observable<Notificacao[]> {
    return this.http.get<Notificacao[]>(`${this.baseUrl}/nao-lidas?usuarioId=${usuarioId}`);
  }

  contagem(usuarioId: number): Observable<{ naoLidas: number }> {
    return this.http.get<{ naoLidas: number }>(`${this.baseUrl}/contagem?usuarioId=${usuarioId}`);
  }

  marcarComoLida(id: number): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/${id}/lida`, {});
  }

  marcarTodasComoLidas(usuarioId: number): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/lidas?usuarioId=${usuarioId}`, {});
  }
}
