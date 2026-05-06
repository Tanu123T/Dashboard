import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },

  {
    path: 'dashboard',
    loadComponent: () =>
      import('./layout/layout.component').then(m => m.LayoutComponent),
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
  }
];