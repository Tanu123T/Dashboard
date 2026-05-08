import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import keycloak from '../../keycloak.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(private router: Router) { }

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> | boolean {
    // If Keycloak reports authenticated, allow. If roles are specified on the route, enforce them.
    const kc = keycloak as any;
    const authenticated = !!(kc && (kc.authenticated || kc.token));

    if (!authenticated) {
      const url = window.location.href;
      if (url.includes('code=') || url.includes('state=')) {
        // Let Keycloak finish processing the auth callback
        return true;
      }
      return this.triggerLogin(kc, state.url);
    }

    // Keep session fresh; if refresh fails, re-authenticate
    if (kc && typeof kc.updateToken === 'function') {
      return kc.updateToken(30)
        .then(() => this.hasRequiredRoles(kc, route))
        .catch(() => this.triggerLogin(kc, state.url));
    }

    return this.hasRequiredRoles(kc, route);
  }

  private triggerLogin(kc: any, redirectUrl: string): boolean {
    const now = Date.now();
    const inProgressRaw = sessionStorage.getItem('kc_login_in_progress');
    const inProgressAt = inProgressRaw ? Number(inProgressRaw) : 0;
    if (inProgressAt && now - inProgressAt < 15000) {
      return false;
    }

    const cleanPath = redirectUrl.split('#')[0].split('?')[0];
    const safePath = cleanPath.startsWith('/') ? cleanPath : `/${cleanPath}`;
    const redirectUri = window.location.origin + safePath;
    if (kc && typeof kc.login === 'function') {
      sessionStorage.setItem('kc_login_in_progress', String(now));
      kc.login({ redirectUri });
      return false;
    }

    this.router.navigate(['/login'], { queryParams: { redirect: safePath } });
    return false;
  }

  private hasRequiredRoles(kc: any, route: ActivatedRouteSnapshot): boolean {
    const requiredRoles: string[] = route.data?.['roles'] || [];
    if (requiredRoles.length === 0) return true;

    // Use Keycloak helper if available
    for (const role of requiredRoles) {
      try {
        if (kc.hasRealmRole && kc.hasRealmRole(role)) {
          return true;
        }
      } catch (e) {
        // ignore and fall back to token parsing
      }
    }

    // Fallback: inspect tokenParsed.realm_access.roles
    const tokenParsed = kc.tokenParsed || {};
    const realmRoles: string[] = tokenParsed?.realm_access?.roles || [];
    const resourceAccess = tokenParsed?.resource_access || {};
    const clientId = kc.clientId;
    const clientRoles: string[] = resourceAccess?.[clientId]?.roles || [];
    const allRoles = [...realmRoles, ...clientRoles];
    const has = requiredRoles.some(r => allRoles.includes(r));
    if (!has) {
      this.router.navigate(['/dashboard']);
      return false;
    }

    return true;
  }
}
