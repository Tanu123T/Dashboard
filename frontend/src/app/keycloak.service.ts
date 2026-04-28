import Keycloak from 'keycloak-js';

const keycloak = new Keycloak({
  url: 'http://localhost:8082',
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