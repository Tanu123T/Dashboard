import { LoginComponent } from './features/auth/login/login.component';

export const routes = [
  { path: 'login', component: LoginComponent },
  { path: '', redirectTo: 'login', pathMatch: 'full' }
];