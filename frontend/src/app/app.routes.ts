import { Routes } from '@angular/router';
import { TaskList } from './components/task-list/task-list';
import { TaskForm } from './components/task-form/task-form';
import { Login } from './components/login/login';
import { UsuarioList } from './components/usuario-list/usuario-list';
import { UsuarioForm } from './components/usuario-form/usuario-form';
import { CategoriaList } from './components/categoria-list/categoria-list';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: '', component: TaskList },
  { path: 'nova', component: TaskForm },
  { path: 'editar/:id', component: TaskForm },
  { path: 'categorias', component: CategoriaList },
  { path: 'usuarios', component: UsuarioList },
  { path: 'usuarios/novo', component: UsuarioForm },
  { path: 'usuarios/editar/:id', component: UsuarioForm }
];
