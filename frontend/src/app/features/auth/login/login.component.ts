import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import keycloak from '../../../keycloak.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit, OnDestroy {
  redirectTo = '/dashboard';
  private handleAuthSuccess = () => {
    this.router.navigateByUrl(this.redirectTo);
  };

  constructor(private route: ActivatedRoute, private router: Router) {}

  ngOnInit() {
    const redirect = this.route.snapshot.queryParamMap.get('redirect');
    if (redirect) this.redirectTo = redirect;

    const kc = keycloak as any;
    if (kc && (kc.authenticated || kc.token)) {
      this.router.navigateByUrl(this.redirectTo);
      return;
    }

    window.addEventListener('kc-auth-success', this.handleAuthSuccess);
  }

  login() {
    const kc = keycloak as any;
    if (kc && typeof kc.login === 'function') {
      kc.login({ redirectUri: window.location.origin + this.redirectTo });
    }
  }

  ngOnDestroy() {
    window.removeEventListener('kc-auth-success', this.handleAuthSuccess);
  }
}
