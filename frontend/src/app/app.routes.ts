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
        path: 'db',
        loadComponent: () => import('./features/db-browser/db-browser.component').then(m => m.DbBrowserComponent)
      },
      {
        path: 'employee-hub',
        loadComponent: () =>
          import('./features/employees/pages/employee-hub.component').then(m => m.EmployeeHubComponent)
      },
      {
        path: 'employee-detail/:id',
        loadComponent: () =>
          import('./features/employees/pages/employee-detail.component').then(m => m.EmployeeDetailComponent)
      },
      {
        path: 'work-calendar',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'org-hierarchy',
        loadComponent: () =>
          import('./features/people-health/pages/org-hierarchy/org-hierarchy.component').then(m => m.OrgHierarchyComponent)
      }
    ]
  },
  { path: '**', redirectTo: 'login' }
];