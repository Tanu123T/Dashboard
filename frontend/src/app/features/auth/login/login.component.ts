import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {

  email: string = '';
  password: string = '';
  rememberMe: boolean = false;

  isLoading = false;

  backgroundElements = Array(5).fill(0);

  constructor(private authService: AuthService) {}

  onSubmit(): void {
  this.authService.login(this.email, this.password)
    .subscribe((res) => {

      if (res.success) {
        localStorage.setItem('token', res.token);
        alert(res.message);
      } else {
        alert(res.message);
      }

    });
}

  getBackgroundStyle(index: number) {
    const isEven = index % 2 === 0;
    return {
      background: isEven 
        ? 'radial-gradient(circle, rgba(56, 189, 248, 0.15) 0%, transparent 70%)'
        : 'radial-gradient(circle, rgba(20, 184, 166, 0.12) 0%, transparent 70%)',
      width: `${300 + index * 100}px`,
      height: `${300 + index * 100}px`,
      left: `${index * 20}%`,
      top: `${index * 15}%`
    };
  }
}