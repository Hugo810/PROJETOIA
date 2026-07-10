import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Comentario } from '../models/collaboration';

@Injectable({ providedIn: 'root' })
export class ComentarioService {
  private baseUrl = '/api';

  constructor(private http: HttpClient) {}

  listarPorTarefa(tarefaId: number): Observable<Comentario[]> {
    return this.http.get<Comentario[]>(`${this.baseUrl}/tarefas/${tarefaId}/comentarios`);
  }

  criar(tarefaId: number, texto: string): Observable<Comentario> {
    return this.http.post<Comentario>(`${this.baseUrl}/tarefas/${tarefaId}/comentarios`, { texto, tarefaId });
  }

  atualizar(id: number, texto: string): Observable<Comentario> {
    return this.http.put<Comentario>(`${this.baseUrl}/comentarios/${id}`, { texto });
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/comentarios/${id}`);
  }
}
