import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor
} from '@angular/common/http';
import { Observable } from 'rxjs';
import keycloak from '../../keycloak.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor() { }

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    // Get token from Keycloak if available
    const token = keycloak?.token;
    
    if (token) {
      // Clone request and add Authorization header
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
      console.log('Token added to request:', token.substring(0, 20) + '...');
    } else {
      console.warn('No token available in interceptor');
    }
    
    return next.handle(request);
  }
}
