import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of, timer, switchMap, tap } from 'rxjs';
import { Categoria } from '../models/category';
import { Usuario } from '../models/user';
import { TaskService } from './task.service';
import { UsuarioService } from './usuario.service';

@Injectable({ providedIn: 'root' })
export class CacheService {
  private categoriasSubject = new BehaviorSubject<Categoria[] | null>(null);
  private usuariosSubject = new BehaviorSubject<Usuario[] | null>(null);
  private categoriaCarregada = 0;
  private usuarioCarregada = 0;
  private readonly TTL = 5 * 60 * 1000;

  constructor(
    private taskService: TaskService,
    private usuarioService: UsuarioService
  ) {}

  getCategorias(): Observable<Categoria[]> {
    const agora = Date.now();
    if (this.categoriasSubject.value && (agora - this.categoriaCarregada) < this.TTL) {
      return this.categoriasSubject.asObservable() as Observable<Categoria[]>;
    }
    this.categoriaCarregada = agora;
    return this.taskService.listarCategorias().pipe(
      tap(data => this.categoriasSubject.next(data))
    );
  }

  getUsuarios(): Observable<Usuario[]> {
    const agora = Date.now();
    if (this.usuariosSubject.value && (agora - this.usuarioCarregada) < this.TTL) {
      return this.usuariosSubject.asObservable() as Observable<Usuario[]>;
    }
    this.usuarioCarregada = agora;
    return this.usuarioService.listar().pipe(
      tap(data => this.usuariosSubject.next(data))
    );
  }

  clear() {
    this.categoriasSubject.next(null);
    this.usuariosSubject.next(null);
    this.categoriaCarregada = 0;
    this.usuarioCarregada = 0;
  }
}
