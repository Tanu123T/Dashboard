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

  // Visual Properties
  progressRingRadius = 70;
  progressCircumference = 2 * Math.PI * 70;
  sprintRoadmapDots: SprintRoadmapDot[] = [];
  completedSprints = 0;
  activeSprintNumber: number | null = null;

  stats: ProjectStats = { total: 0, complete: 0, inProgress: 0, delayed: 0 };
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
    this.projectsService.getAllProjects()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (projects) => {
          this.projects = projects;
          this.stats = this.projectsService.getStats();
          this.loading = false;
          
          this.route.params.pipe(takeUntil(this.destroy$)).subscribe(params => {
            if (params['id']) {
              this.loadProjectDetail(parseInt(params['id'], 10));
            } else {
              this.selectedProject = null;
            }
          });
        },
        error: () => {
          this.error = 'Failed to load projects.';
          this.loading = false;
        }
      });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadProjectDetail(id: number) {
    const found = this.projects.find(p => p.id === id);
    if (found) {
      this.selectedProject = found;
      this.calculateSprintTimeline();
    } else {
      this.projectsService.getProjectById(id).subscribe(p => {
        this.selectedProject = p;
        this.calculateSprintTimeline();
      });
    }
  }

  calculateSprintTimeline() {
    if (!this.selectedProject) return;
    const progress = this.selectedProject.progress;
    const total = this.selectedProject.totalPlannedSprints;
    
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

  openProjectDetail(projectId: number) { this.router.navigate(['/dashboard/projects', projectId]); }
  backToProjects() { this.router.navigate(['/dashboard/projects']); }
}