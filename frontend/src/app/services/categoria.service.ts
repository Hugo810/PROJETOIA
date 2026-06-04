import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Categoria } from '../models/category';

@Injectable({ providedIn: 'root' })
export class CategoriaService {
  private baseUrl = '/api/categorias';

  constructor(private http: HttpClient) {}

  listar(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(this.baseUrl);
  }

  criar(nome: string): Observable<Categoria> {
    return this.http.post<Categoria>(this.baseUrl, { nome });
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
