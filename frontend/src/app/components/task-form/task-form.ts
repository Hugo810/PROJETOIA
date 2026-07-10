import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { TaskService } from '../../services/task.service';
import { AuthService } from '../../services/auth.service';
import { CacheService } from '../../services/cache.service';
import { Categoria } from '../../models/category';
import { Usuario } from '../../models/user';
import { TaskComments } from '../task-comments/task-comments';
import { TaskHistory } from '../task-history/task-history';
import { TaskTimer } from '../task-timer/task-timer';

@Component({
  selector: 'app-task-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, TaskComments, TaskHistory, TaskTimer],
  templateUrl: './task-form.html',
  styleUrl: './task-form.css'
})
export class TaskForm implements OnInit {
  categorias: Categoria[] = [];
  usuarios: Usuario[] = [];
  form: FormGroup;
  editando = false;
  tarefaId?: number;
  today: string = new Date().toISOString().split('T')[0];

  constructor(
    private fb: FormBuilder,
    private taskService: TaskService,
    private auth: AuthService,
    private cache: CacheService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.form = this.fb.group({
      titulo: ['', [Validators.required, Validators.maxLength(200)]],
      descricao: [''],
      categoriaId: [0, Validators.required],
      prazo: ['', Validators.required],
      prioridade: ['MEDIA', Validators.required],
      responsavelId: ['']
    });
  }

  ngOnInit() {
    this.cache.getCategorias().subscribe(data => this.categorias = data);

    if (this.podeDistribuir) {
      this.cache.getUsuarios().subscribe(data => this.usuarios = data);
    }

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.editando = true;
      this.tarefaId = +id;
      this.taskService.buscarTarefa(this.tarefaId).subscribe(data => {
        if (data.status === 'CONCLUIDA') {
          this.router.navigate(['/']);
          return;
        }
        this.form.patchValue({
          titulo: data.titulo,
          descricao: data.descricao,
          categoriaId: data.categoriaId,
          prazo: data.prazo,
          prioridade: data.prioridade || 'MEDIA',
          responsavelId: data.responsavelId || ''
        });
      });
    }
  }

  get podeDistribuir(): boolean {
    return this.auth.temRole('ADMIN', 'DISTRIBUIDOR');
  }

  salvar() {
    if (this.form.invalid) return;

    const value = { ...this.form.value };
    value.titulo = value.titulo?.toUpperCase();
    value.descricao = value.descricao?.toUpperCase();
    if (!value.responsavelId) delete value.responsavelId;

    const obs = this.editando
      ? this.taskService.atualizarTarefa(this.tarefaId!, value)
      : this.taskService.criarTarefa(value);

    obs.subscribe({
      next: () => this.router.navigate(['/']),
      error: (err) => this.form.setErrors({ api: err.error?.erro || 'Erro ao salvar tarefa.' })
    });
  }

  getError(field: string): string {
    const control = this.form.get(field);
    if (control?.touched && control.invalid) {
      if (control.hasError('required')) {
        if (field === 'prazo') return 'Prazo é obrigatório.';
        if (field === 'prioridade') return 'Prioridade é obrigatória.';
        return 'Título é obrigatório.';
      }
      if (control.hasError('maxlength')) return 'Máximo de 200 caracteres.';
    }
    if (field === 'prazo' && control?.value && control.value < this.today) {
      return 'Prazo deve ser hoje ou uma data futura.';
    }
    return '';
  }
}
