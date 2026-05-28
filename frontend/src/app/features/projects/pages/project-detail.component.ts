import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ProjectsService, Project, ProjectStats } from '../../../services/projects.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

interface SprintRoadmapDot {
  id: number;
  status: 'completed' | 'active' | 'planned';
}

@Component({
  selector: 'app-project-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './project-detail.component.html',
  styleUrls: ['./project-detail.component.css']
})
export class ProjectDetailComponent implements OnInit, OnDestroy {
  project: Project | null = null;
  pageLastUpdated: string = '';
  loading = true;
  error: string | null = null;

  sprintRoadmapDots: SprintRoadmapDot[] = [];
  completedSprints = 0;
  activeSprintNumber: number | null = null;

  private destroy$ = new Subject<void>();

  constructor(
    private projectsService: ProjectsService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.updateLastUpdated();
  }

  ngOnInit() {
    this.loading = true;
    const projectId = this.route.snapshot.params['id'];
    
    if (projectId) {
      this.projectsService.getProjectById(parseInt(projectId, 10))
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (project) => {
            this.project = project;
            this.calculateSprintTimeline();
            this.loading = false;
          },
          error: () => {
            this.error = 'Failed to load project details.';
            this.loading = false;
          }
        });
    }
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  calculateSprintTimeline() {
    if (!this.project) return;
    const progress = this.project.progress;
    const total = this.project.totalPlannedSprints;
    
    this.completedSprints = Math.floor((progress / 100) * total);
    this.activeSprintNumber = this.completedSprints < total ? this.completedSprints + 1 : null;

    this.sprintRoadmapDots = Array.from({ length: total }, (_, i) => ({
      id: i + 1,
      status: (i + 1) <= this.completedSprints ? 'completed' : (i + 1) === this.activeSprintNumber ? 'active' : 'planned'
    }));
  }

  updateLastUpdated() {
    const now = new Date();
    this.pageLastUpdated = now.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }) + 
                          ', ' + now.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
  }

  backToProjects() {
    this.router.navigate(['/dashboard/projects']);
  }

  viewSprint() {
    if (this.activeSprintNumber) {
      this.router.navigate(['/dashboard/sprints', this.activeSprintNumber]);
    }
  }
}
