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
  sprints: any[] = [];
  stats = [
    { label: 'Total Planned Sprints', value: '0', icon: '⚡', color: '#5B7CFF' },
    { label: 'Completed', value: '0', icon: '✓', color: '#1AB394' },
    { label: 'Active', value: '0', icon: '⏱', color: '#FFA940' },
    { label: 'Avg. Completion', value: '0%', icon: '📈', color: '#00BCD4' },
    { label: 'Team Size', value: '4', icon: '👥', color: '#9B59B6' }
  ];

  constructor() { }
}
