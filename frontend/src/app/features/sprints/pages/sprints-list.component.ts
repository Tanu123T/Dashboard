import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SprintFeatureService } from '../services/sprint-feature.service';
import { Sprint, SprintStats, TeamMember } from '../models/sprint.model';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Router } from '@angular/router';

@Component({
  selector: 'app-sprints-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './sprints-list.component.html',
  styleUrls: ['./sprints-list.component.css']
})
export class SprintsListComponent implements OnInit, OnDestroy {
  sprints: Sprint[] = [];
  teamMembers: TeamMember[] = [];
  stats: SprintStats = {
    totalPlanned: 0,
    completed: 0,
    active: 0,
    avgCompletion: 0,
    teamSize: 0
  };
  loading = true;
  error: string | null = null;
  pageLastUpdated: string = '';
  private destroy$ = new Subject<void>();

  constructor(
    private sprintService: SprintFeatureService,
    private router: Router
  ) {
    this.updateLastUpdated();
  }

  ngOnInit() {
    this.loading = true;
    this.error = null;
    
    // Fetch sprint dashboard data from API
    this.sprintService.getAllSprints()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: any) => {
          // Response structure: { summary, team, sprints }
          if (response && response.sprints) {
            // Map backend sprint response to frontend Sprint model
            this.sprints = response.sprints.map((sprint: any) => ({
              id: sprint.id,
              name: sprint.name,
              projectName: sprint.projectName,
              startDate: this.formatDate(sprint.startDate),
              endDate: this.formatDate(sprint.endDate),
              status: sprint.status?.toLowerCase() || 'planned',
              progress: sprint.progress || 0,
              completedTasks: sprint.completedTasks || 0,
              totalTasks: sprint.totalTasks || 0
            }));
          }

          // Extract and set stats from summary
          if (response && response.summary) {
            this.stats = {
              totalPlanned: response.summary.totalPlanned || 0,
              completed: response.summary.completed || 0,
              active: response.summary.active || 0,
              avgCompletion: response.summary.avgCompletion || 0,
              teamSize: response.summary.teamSize || 0
            };
          }

          // Extract and set team members
          if (response && response.team && Array.isArray(response.team)) {
            this.teamMembers = response.team.map((member: any) => ({
              id: member.id || 0,
              name: member.name || '',
              role: member.role || '',
              color: this.generateColorForMember(member.name || '')
            }));
          }

          this.loading = false;
        },
        error: (err: any) => {
          console.error('Error loading sprint data:', err);
          this.error = `Failed to load sprint data: ${err.status === 0 ? 'Backend server is not running' : err.message}`;
          this.loading = false;
        }
      });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  updateLastUpdated() {
    const now = new Date();
    this.pageLastUpdated = now.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }) + 
                          ', ' + now.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
  }

  private formatDate(date: any): string {
    if (!date) return '';
    const d = new Date(date);
    return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  }

  private generateColorForMember(name: string): string {
    const colors = ['#3b82f6', '#1e40af', '#0284c7', '#0369a1', '#0c4a6e', '#164e63', '#1e3a8a', '#1e40af'];
    const index = name.charCodeAt(0) % colors.length;
    return colors[index];
  }

  getStatusClass(status: string): string {
    return status === 'completed' ? 'completed' : status === 'active' ? 'active' : 'planned';
  }

  getInitials(name: string): string {
    return name.split(' ').map(n => n[0]).join('').toUpperCase();
  }

  openSprintDetail(sprintId: number) {
    this.router.navigate(['/dashboard/sprints', sprintId]);
  }
}
