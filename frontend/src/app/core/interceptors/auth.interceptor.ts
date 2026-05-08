import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor
} from '@angular/common/http';
import { Observable, from } from 'rxjs';
import { mergeMap } from 'rxjs/operators';
import keycloak from '../../keycloak.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor() { }

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    // Prefer token from Keycloak adapter, fall back to sessionStorage
    const storedToken = sessionStorage.getItem('kc_token');
    const token = keycloak?.token || storedToken;

    // If Keycloak adapter is present, attempt a background refresh when token is near expiry
    if (keycloak && typeof (keycloak as any).updateToken === 'function') {
      return from((keycloak as any).updateToken(30)).pipe(mergeMap(() => {
        const newToken = keycloak?.token || sessionStorage.getItem('kc_token');
        if (newToken) {
          request = request.clone({ setHeaders: { Authorization: `Bearer ${newToken}` } });
          console.log('Token added to request after refresh:', (newToken as string).substring(0, 20) + '...');
        } else if (storedToken) {
          request = request.clone({ setHeaders: { Authorization: `Bearer ${storedToken}` } });
        } else {
          console.warn('No token available in interceptor after refresh');
        }
        return next.handle(request);
      }));
    }

    if (token) {
      request = request.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
      console.log('Token added to request:', token.substring(0, 20) + '...');
    } else {
      console.warn('No token available in interceptor');
    }

    return next.handle(request);
  }
}
