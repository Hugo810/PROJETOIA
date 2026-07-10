import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Projeto, Meta } from '../models/projeto';

@Injectable({ providedIn: 'root' })
export class ProjetoService {
  private baseUrl = '/api/projetos';

  constructor(private http: HttpClient) {}

  listar(): Observable<Projeto[]> {
    return this.http.get<Projeto[]>(this.baseUrl);
  }

  buscar(id: number): Observable<Projeto> {
    return this.http.get<Projeto>(`${this.baseUrl}/${id}`);
  }

  criar(dados: Partial<Projeto>): Observable<Projeto> {
    return this.http.post<Projeto>(this.baseUrl, dados);
  }

  atualizar(id: number, dados: Partial<Projeto>): Observable<Projeto> {
    return this.http.put<Projeto>(`${this.baseUrl}/${id}`, dados);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  listarMetas(projetoId: number): Observable<Meta[]> {
    return this.http.get<Meta[]>(`${this.baseUrl}/${projetoId}/metas`);
  }

  criarMeta(projetoId: number, dados: Partial<Meta>): Observable<Meta> {
    return this.http.post<Meta>(`${this.baseUrl}/${projetoId}/metas`, dados);
  }

  atualizarMeta(metaId: number, dados: Partial<Meta>): Observable<Meta> {
    return this.http.put<Meta>(`${this.baseUrl}/metas/${metaId}`, dados);
  }

  excluirMeta(metaId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/metas/${metaId}`);
  }
}
