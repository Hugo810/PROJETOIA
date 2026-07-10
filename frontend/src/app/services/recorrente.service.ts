import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TarefaRecorrente } from '../models/recorrente';

@Injectable({ providedIn: 'root' })
export class RecorrenteService {
  private baseUrl = '/api/tarefas-recorrentes';

  constructor(private http: HttpClient) {}

  listar(): Observable<TarefaRecorrente[]> {
    return this.http.get<TarefaRecorrente[]>(this.baseUrl);
  }

  criar(dados: Partial<TarefaRecorrente>): Observable<TarefaRecorrente> {
    return this.http.post<TarefaRecorrente>(this.baseUrl, dados);
  }

  atualizar(id: number, dados: Partial<TarefaRecorrente>): Observable<TarefaRecorrente> {
    return this.http.put<TarefaRecorrente>(`${this.baseUrl}/${id}`, dados);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  pular(id: number): Observable<TarefaRecorrente> {
    return this.http.patch<TarefaRecorrente>(`${this.baseUrl}/${id}/pular`, {});
  }

  adiar(id: number, dias: number): Observable<TarefaRecorrente> {
    return this.http.patch<TarefaRecorrente>(`${this.baseUrl}/${id}/adiar?dias=${dias}`, {});
  }

  toggleAtiva(id: number, ativa: boolean): Observable<TarefaRecorrente> {
    return this.http.patch<TarefaRecorrente>(`${this.baseUrl}/${id}/ativa`, ativa);
  }

  proximasExecucoes(id: number, quantidade: number = 6): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/${id}/proximas?quantidade=${quantidade}`);
  }
}
