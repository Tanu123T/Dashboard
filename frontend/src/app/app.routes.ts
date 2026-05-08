import { Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },

  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },

  {
    path: 'dashboard',
    loadComponent: () =>
      import('./layout/layout.component').then(m => m.LayoutComponent),
    canActivate: [AuthGuard],
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'projects',
        loadComponent: () =>
          import('./features/projects/projects.component').then(m => m.ProjectsComponent)
      },
      {
        path: 'projects/:id',
        loadComponent: () =>
          import('./features/projects/projects.component').then(m => m.ProjectsComponent)
      },
      {
        path: 'sprints',
        loadComponent: () =>
          import('./features/sprints/pages/sprints-list.component').then(m => m.SprintsListComponent)
      },
      {
        path: 'sprints/:id',
        loadComponent: () =>
          import('./features/sprints/pages/sprint-detail.component').then(m => m.SprintDetailComponent)
      },
      {
        path: 'members/:projectId/:member',
        loadComponent: () =>
          import('./features/sprints/pages/member-detail.component').then(m => m.MemberDetailComponent)
      },

      {
        path: 'workforce-health',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'employee-hub',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'work-calendar',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'org-hierarchy',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
      }
    ]
  },
  { path: '**', redirectTo: 'login' }
];