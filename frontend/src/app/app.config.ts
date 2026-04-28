import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { APP_INITIALIZER } from '@angular/core';
import { initKeycloak } from './keycloak.service';
import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
 providers: [
  provideRouter(routes),
  {
    provide: APP_INITIALIZER,
    useFactory: initKeycloak,
    multi: true
  }
],
};
