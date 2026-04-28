import Keycloak from 'keycloak-js';
import { environment } from 'src/environments/environment';

const keycloak = new Keycloak({
 url: 'http://192.168.10.136:8082',
  realm: 'ceo-dashboard',
  clientId: 'ceo-backend'
});

export function initKeycloak() {
  return () =>
    keycloak.init({
      onLoad: 'login-required',
      checkLoginIframe: false
    });
}

export default keycloak;