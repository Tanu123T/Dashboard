import Keycloak from 'keycloak-js';
import { environment } from 'src/environments/environment';

const keycloak = new Keycloak({
  url: 'http://192.168.10.136:8082',
  realm: 'ceo-dashboard',
  clientId: 'ceo-backend'
});

export function initKeycloak() {
  return () => {
    console.log('Initializing Keycloak...');
    return keycloak.init({
      onLoad: 'check-sso',
      checkLoginIframe: false,
      silentCheckSsoRedirectUri: window.location.origin + '/assets/silent-check-sso.html'
    }).then(authenticated => {
      console.log('Keycloak initialized. Authenticated:', authenticated);
      return true;
    }).catch(error => {
      console.error('Keycloak initialization error:', error);
      // Allow app to continue loading even if Keycloak fails
      return true;
    });
  };
}

export default keycloak;