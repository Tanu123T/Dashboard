import { Component } from '@angular/core';

@Component({
  selector: 'app-button',
  template: `<button class="btn" [class.btn-primary]="primary"><ng-content></ng-content></button>`,
  styles: [`
    .btn {
      padding: 0.5rem 1rem;
      border: none;
      border-radius: 4px;
      cursor: pointer;
    }
    .btn-primary {
      background-color: #007bff;
      color: white;
    }
  `]
})
export class ButtonComponent {
  primary = false;
}
