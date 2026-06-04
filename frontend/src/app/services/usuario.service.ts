import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Usuario } from '../models/user';

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private baseUrl = '/api/usuarios';

  constructor(private http: HttpClient) {}

  listar(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(this.baseUrl);
  }

  criar(dto: Partial<Usuario>): Observable<Usuario> {
    return this.http.post<Usuario>(this.baseUrl, dto);
  }

  atualizar(id: number, dto: Partial<Usuario>): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.baseUrl}/${id}`, dto);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
