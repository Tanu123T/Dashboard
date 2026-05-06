import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { SprintFeatureService } from '../services/sprint-feature.service';
import { Sprint } from '../models/sprint.model';

interface TaskItem {
  id: number;
  name: string;
  progress: number;
}

interface TeamMemberWork {
  name: string;
  assigned: number;
  inProgress: number;
  completed: number;
}

@Component({
  selector: 'app-sprint-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './sprint-detail.component.html',
  styleUrls: ['./sprint-detail.component.css']
})
export class SprintDetailComponent implements OnInit, OnDestroy {
  sprint: Sprint | null = null;
  sprintId: number | null = null;
  loading = true;
  error: string | null = null;
  pageLastUpdated: string = '';
  private destroy$ = new Subject<void>();

  // Data from API
  taskProgress: TaskItem[] = [];
  workDistribution: TeamMemberWork[] = [];
  burndownDays = ['D1', 'D2', 'D3', 'D4', 'D5', 'D6', 'D7', 'D8', 'D9'];
  estimatedTimeUsage = 0;

  constructor(
    private sprintService: SprintFeatureService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.updateLastUpdated();
  }

  ngOnInit() {
    this.route.params.pipe(takeUntil(this.destroy$)).subscribe(params => {
      if (params['id']) {
        this.sprintId = parseInt(params['id'], 10);
        this.loadSprintDetail(this.sprintId);
      }
    });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadSprintDetail(id: number) {
    this.loading = true;
    this.error = null;
    
    this.sprintService.getSprintDetail(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: any) => {
          // Map API response to Sprint model
          if (response) {
            this.sprint = {
              id: response.id,
              name: response.name,
              projectName: response.projectName,
              startDate: this.formatDate(response.startDate),
              endDate: this.formatDate(response.endDate),
              status: (response.status || 'planned').toLowerCase() as 'completed' | 'active' | 'planned',
              progress: response.progress || 0,
              completedTasks: response.completedTasks || 0,
              totalTasks: response.totalTasks || 0
            };

            // Extract task progress data
            if (response.tasks && Array.isArray(response.tasks)) {
              this.taskProgress = response.tasks.map((task: any, index: number) => ({
                id: task.id || index + 1,
                name: task.name || `Task ${index + 1}`,
                progress: task.progress || 0
              }));
            }

            // Extract member work distribution
            if (response.memberWork && Array.isArray(response.memberWork)) {
              this.workDistribution = response.memberWork.map((member: any) => ({
                name: member.name || '',
                assigned: member.assigned || 0,
                inProgress: member.inProgress || 0,
                completed: member.completed || 0
              }));
            }

            // Set time usage
            if (response.timeUsedPercentage !== undefined) {
              this.estimatedTimeUsage = response.timeUsedPercentage;
            }
          }

          this.loading = false;
        },
        error: (err: any) => {
          console.error('Error loading sprint detail:', err);
          this.error = `Failed to load sprint details: ${err.status === 0 ? 'Backend server is not running' : err.message}`;
          this.loading = false;
        }
      });
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

  backToSprints() {
    this.router.navigate(['/dashboard/sprints']);
  }

  getProgressBarColor(progress: number): string {
    if (progress === 100) return '#10b981';
    if (progress >= 75) return '#f59e0b';
    return '#ef4444';
  }

  getWorkColor(completed: number, assigned: number): string {
    if (assigned === 0) return '#e2e8f0';
    const percentage = (completed / assigned) * 100;
    if (percentage === 100) return '#10b981';
    if (percentage >= 50) return '#f59e0b';
    return '#ef4444';
  }
}
