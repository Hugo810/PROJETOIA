import { Routes } from '@angular/router';
import { TaskList } from './components/task-list/task-list';
import { TaskForm } from './components/task-form/task-form';
import { Login } from './components/login/login';
import { UsuarioList } from './components/usuario-list/usuario-list';
import { UsuarioForm } from './components/usuario-form/usuario-form';
import { CategoriaList } from './components/categoria-list/categoria-list';
import { DailyTasks } from './components/daily-tasks/daily-tasks';
import { RecurringTasks } from './components/recurring-tasks/recurring-tasks';
import { AutomationRules } from './components/automation-rules/automation-rules';
import { DashboardPage } from './components/dashboard/dashboard';
import { GanttChart } from './components/gantt-chart/gantt-chart';
import { ProjectList } from './components/project-list/project-list';
import { ProjectBoard } from './components/project-board/project-board';
import { ExportImport } from './components/export-import/export-import';
import { authGuard, roleGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: '', component: TaskList, canActivate: [authGuard] },
  { path: 'hoje', component: DailyTasks, canActivate: [authGuard] },
  { path: 'recorrentes', component: RecurringTasks, canActivate: [authGuard] },
  { path: 'automacoes', component: AutomationRules, canActivate: [authGuard] },
  { path: 'dashboard', component: DashboardPage, canActivate: [authGuard] },
  { path: 'gantt', component: GanttChart, canActivate: [authGuard] },
  { path: 'nova', component: TaskForm, canActivate: [authGuard] },
  { path: 'editar/:id', component: TaskForm, canActivate: [authGuard] },
  { path: 'categorias', component: CategoriaList, canActivate: [authGuard] },
  { path: 'projetos', component: ProjectList, canActivate: [authGuard] },
  { path: 'projetos/:id', component: ProjectBoard, canActivate: [authGuard] },
  { path: 'exportar-importar', component: ExportImport, canActivate: [authGuard] },
  { path: 'usuarios', component: UsuarioList, canActivate: [authGuard] },
  { path: 'usuarios/novo', component: UsuarioForm, canActivate: [authGuard, roleGuard('ADMIN')] },
  { path: 'usuarios/editar/:id', component: UsuarioForm, canActivate: [authGuard, roleGuard('ADMIN')] },
  { path: '**', redirectTo: '' }
];
