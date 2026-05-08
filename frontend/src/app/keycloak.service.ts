import Keycloak from 'keycloak-js';
import { environment } from 'src/environments/environment';

const keycloak = new Keycloak({
  url: environment.keycloakUrl,
  realm: environment.keycloakRealm,
  clientId: environment.keycloakClientId
});

export function initKeycloak() {
  return () => {
    console.log('Initializing Keycloak...');
    return keycloak.init({
      onLoad: 'check-sso',
      responseMode: 'query',
      pkceMethod: 'S256',
      checkLoginIframe: false,
      silentCheckSsoRedirectUri: window.location.origin + '/assets/silent-check-sso.html'
    }).then(authenticated => {
      console.log('Keycloak initialized. Authenticated:', authenticated);

      // Persist token to sessionStorage so interceptor/guards can read it if adapter state is lost
      if (keycloak?.token) {
        sessionStorage.setItem('kc_token', keycloak.token);
      }

      // Attach lifecycle handlers to keep storage in sync
      const previousAuthSuccess = keycloak.onAuthSuccess;
      keycloak.onAuthSuccess = () => {
        if (typeof previousAuthSuccess === 'function') previousAuthSuccess();
        if (keycloak?.token) sessionStorage.setItem('kc_token', keycloak.token);
        sessionStorage.removeItem('kc_login_in_progress');
        // Remove Keycloak auth fragments to prevent redirect loops
        if (window.location.hash && window.location.hash.includes('state=')) {
          history.replaceState(null, '', window.location.pathname + window.location.search);
        }
        window.dispatchEvent(new CustomEvent('kc-auth-success'));
      };

      const previousAuthError = keycloak.onAuthError;
      keycloak.onAuthError = () => {
        if (typeof previousAuthError === 'function') previousAuthError();
        sessionStorage.removeItem('kc_login_in_progress');
      };

      keycloak.onAuthRefreshSuccess = () => {
        if (keycloak?.token) sessionStorage.setItem('kc_token', keycloak.token);
      };

      keycloak.onAuthRefreshError = () => {
        sessionStorage.removeItem('kc_token');
      };

      keycloak.onAuthLogout = () => {
        sessionStorage.removeItem('kc_token');
      };

      keycloak.onTokenExpired = () => {
        // Attempt a background refresh and persist new token
        keycloak.updateToken(30).then(() => {
          if (keycloak?.token) sessionStorage.setItem('kc_token', keycloak.token);
        }).catch(() => {
          sessionStorage.removeItem('kc_token');
        });
      };

      return true;
    }).catch(error => {
      console.error('Keycloak initialization error:', error);
      // Allow app to continue loading even if Keycloak fails
      return true;
    });
  };
}

export default keycloak;