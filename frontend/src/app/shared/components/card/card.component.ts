import { Component } from '@angular/core';

@Component({
  selector: 'app-card',
  template: `<div class="card"><ng-content></ng-content></div>`,
  styles: [`
    .card {
      border: 1px solid #ddd;
      border-radius: 4px;
      padding: 1rem;
      margin: 1rem 0;
    }
  `]
})
export class CardComponent {
}
