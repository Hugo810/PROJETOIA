import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Tarefa } from '../models/task';
import { Categoria } from '../models/category';

@Injectable({ providedIn: 'root' })
export class TaskService {
  private baseUrl = '/api';

  constructor(private http: HttpClient) {}

  listarTarefas(status?: string, categoriaId?: number, responsavelId?: number, pagina = 0, tamanho = 10): Observable<any> {
    let params = new HttpParams()
      .set('pagina', pagina.toString())
      .set('tamanho', tamanho.toString());
    if (status) params = params.set('status', status);
    if (categoriaId) params = params.set('categoriaId', categoriaId.toString());
    if (responsavelId) params = params.set('responsavelId', responsavelId.toString());
    return this.http.get<any>(`${this.baseUrl}/tarefas`, { params });
  }

  buscarTarefa(id: number): Observable<Tarefa> {
    return this.http.get<Tarefa>(`${this.baseUrl}/tarefas/${id}`);
  }

  criarTarefa(tarefa: Tarefa): Observable<Tarefa> {
    return this.http.post<Tarefa>(`${this.baseUrl}/tarefas`, tarefa);
  }

  atualizarTarefa(id: number, tarefa: Tarefa): Observable<Tarefa> {
    return this.http.put<Tarefa>(`${this.baseUrl}/tarefas/${id}`, tarefa);
  }

  iniciarTarefa(id: number): Observable<Tarefa> {
    return this.http.patch<Tarefa>(`${this.baseUrl}/tarefas/${id}/iniciar`, {});
  }

  concluirTarefa(id: number): Observable<Tarefa> {
    return this.http.patch<Tarefa>(`${this.baseUrl}/tarefas/${id}/concluir`, {});
  }

  distribuirTarefa(id: number, responsavelId: number): Observable<Tarefa> {
    return this.http.patch<Tarefa>(`${this.baseUrl}/tarefas/${id}/distribuir?responsavelId=${responsavelId}`, {});
  }

  excluirTarefa(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/tarefas/${id}`);
  }

  listarCategorias(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(`${this.baseUrl}/categorias`);
  }
}
