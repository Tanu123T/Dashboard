import { Injectable } from '@angular/core';
import { of, delay } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  login(email: string, password: string) {

    if (email === 'admin@gmail.com' && password === '1234') {
      return of({
        success: true,
        token: 'dummy-token',
        message: 'Login successful'
      }).pipe(delay(500));
    }

    return of({
      success: false,
      token: '',
      message: 'Invalid credentials'
    }).pipe(delay(500));
  }
}