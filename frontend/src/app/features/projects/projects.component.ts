import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ProjectsService, Project, ProjectStats } from '../../services/projects.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

interface SprintRoadmapDot {
  id: number;
  status: 'completed' | 'active' | 'planned';
}

@Component({
  selector: 'app-projects',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './projects.component.html',
  styleUrls: ['./projects.component.css']
})
export class ProjectsComponent implements OnInit, OnDestroy {
  projects: Project[] = [];
  selectedProject: Project | null = null;
  pageLastUpdated: string = '';
  projectId: number | null = null;
  loading = true;
  error: string | null = null;

  // For detail view
  progressRingRadius = 66;
  progressCircumference = 0;
  progressDashOffset = 0;
  sprintRoadmapDots: SprintRoadmapDot[] = [];
  completedSprints = 0;
  activeSprintNumber: number | null = null;

  stats: ProjectStats = {
    total: 0,
    complete: 0,
    inProgress: 0,
    delayed: 0
  };

  private destroy$ = new Subject<void>();

  constructor(
    private projectsService: ProjectsService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.updateLastUpdated();
    this.progressCircumference = 2 * Math.PI * this.progressRingRadius;
  }

  ngOnInit() {
    // Load all projects first
    this.loading = true;
    this.projectsService.getAllProjects()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (projects) => {
          this.projects = projects;
          this.stats = this.projectsService.getStats();
          this.loading = false;
          
          // Then check if we need to load a specific project
          this.route.params
            .pipe(takeUntil(this.destroy$))
            .subscribe(params => {
              if (params['id']) {
                this.loadProjectDetail(parseInt(params['id'], 10));
              } else {
                this.selectedProject = null;
                this.projectId = null;
              }
            });
        },
        error: (err) => {
          console.error('Error loading projects:', err);
          this.error = 'Failed to load projects. Please try again later.';
          this.loading = false;
        }
      });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadProjectDetail(id: number) {
    this.projectId = id;
    // First try to find in already loaded projects
    const found = this.projects.find(p => p.id === id);
    if (found) {
      this.selectedProject = found;
      this.calculateSprintTimeline();
    } else {
      // If not found, fetch from API
      this.projectsService.getProjectById(id)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (project) => {
            this.selectedProject = project;
            this.calculateSprintTimeline();
          },
          error: (err) => {
            console.error('Error loading project detail:', err);
            this.selectedProject = null;
          }
        });
    }
  }

  calculateSprintTimeline() {
    if (!this.selectedProject) return;

    const progress = Math.max(0, Math.min(100, this.selectedProject.progress));
    this.progressDashOffset = this.progressCircumference - (progress / 100) * this.progressCircumference;

    const plannedSprints = Math.max(1, this.selectedProject.totalPlannedSprints);
    this.completedSprints = Math.min(
      plannedSprints,
      Math.round((progress / 100) * plannedSprints)
    );
    this.activeSprintNumber = this.completedSprints < plannedSprints ? this.completedSprints + 1 : null;

    this.sprintRoadmapDots = Array.from({ length: plannedSprints }, (_, index) => {
      const sprintId = index + 1;
      const status = sprintId <= this.completedSprints
        ? 'completed'
        : sprintId === this.activeSprintNumber
          ? 'active'
          : 'planned';

      return { id: sprintId, status };
    });
  }

  updateLastUpdated() {
    const now = new Date();
    this.pageLastUpdated = now.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric'
    }) + ', ' + now.toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getStatusBadgeColor(status: string): string {
    switch(status) {
      case 'complete':
        return '#d1fae5';
      case 'in-progress':
        return '#fef3c7';
      case 'delayed':
        return '#fee2e2';
      default:
        return '#f3f4f6';
    }
  }

  getStatusTextColor(status: string): string {
    switch(status) {
      case 'complete':
        return '#047857';
      case 'in-progress':
        return '#b45309';
      case 'delayed':
        return '#b91c1c';
      default:
        return '#374151';
    }
  }

  getProgressBarColor(status: string): string {
    switch(status) {
      case 'complete':
        return '#10b981';
      case 'in-progress':
        return '#f59e0b';
      case 'delayed':
        return '#ef4444';
      default:
        return '#6b7280';
    }
  }

  getStatusLabel(status: string): string {
    switch(status) {
      case 'complete':
        return 'Complete';
      case 'in-progress':
        return 'In Progress';
      case 'delayed':
        return 'Delayed';
      default:
        return 'Unknown';
    }
  }

  openProjectDetail(projectId: number) {
    this.router.navigate(['/dashboard/projects', projectId]);
  }

  backToProjects() {
    this.router.navigate(['/dashboard/projects']);
  }

  getSprintStatusLabel(status: string): string {
    switch(status) {
      case 'completed':
        return 'Done';
      case 'active':
        return 'Active';
      case 'planned':
        return 'Planned';
      default:
        return '';
    }
  }
}
