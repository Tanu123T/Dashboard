import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent {
  sprints = [
    { id: 1, name: 'Sprint 1', status: 'Active', progress: 65 },
    { id: 2, name: 'Sprint 2', status: 'Planning', progress: 20 }
  ];

  constructor() { }
}
