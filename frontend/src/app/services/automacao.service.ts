import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RegraAutomacao } from '../models/automacao';

@Injectable({ providedIn: 'root' })
export class AutomacaoService {
  private baseUrl = '/api/automacoes';

  constructor(private http: HttpClient) {}

  listar(): Observable<RegraAutomacao[]> {
    return this.http.get<RegraAutomacao[]>(this.baseUrl);
  }

  criar(dados: Partial<RegraAutomacao>): Observable<RegraAutomacao> {
    return this.http.post<RegraAutomacao>(this.baseUrl, dados);
  }

  atualizar(id: number, dados: Partial<RegraAutomacao>): Observable<RegraAutomacao> {
    return this.http.put<RegraAutomacao>(`${this.baseUrl}/${id}`, dados);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  toggleAtiva(id: number, ativa: boolean): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/${id}/ativa`, ativa);
  }
}
