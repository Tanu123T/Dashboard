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
          // Calculate actual progress for each project
          this.projects.forEach(project => {
            project.progress = this.calculateProjectProgress(project);
          });
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
    console.log('Loading project detail for ID:', id);
    console.log('Available projects:', this.projects);
    
    const found = this.projects.find(p => p.id === id);
    console.log('Found in cache:', found);
    
    if (found) {
      this.selectedProject = found;
      console.log('Selected project from cache:', this.selectedProject);
      console.log('Description:', this.selectedProject.description);
      console.log('Team:', this.selectedProject.team);
      this.calculateSprintTimeline();
    } else {
      this.projectsService.getProjectById(id).subscribe(p => {
        this.selectedProject = p;
        console.log('Selected project from API:', this.selectedProject);
        console.log('Description:', this.selectedProject.description);
        console.log('Team:', this.selectedProject.team);
        this.calculateSprintTimeline();
      });
    }
  }

  /**
   * Calculate project progress based on three factors:
   * 1. Days elapsed vs total project duration (33%)
   * 2. Completed sprints vs total planned sprints (33%)
   * 3. Visual proportion of completed phases (33%)
   */
  calculateProjectProgress(project: Project): number {
    const daysProgress = this.calculateDaysProgress(project);
    const sprintsProgress = this.calculateSprintsProgress(project);
    const phasesProgress = this.calculatePhasesProgress(project);

    // Average the three metrics
    return Math.round((daysProgress + sprintsProgress + phasesProgress) / 3);
  }

  /**
   * Calculate progress based on calendar days elapsed
   */
  private calculateDaysProgress(project: Project): number {
    const startDate = this.parseDate(project.startDate);
    if (!startDate) return 0;

    let endDate = this.parseDate(project.dueDate);
    if (!endDate) {
      endDate = this.parseDate(project.deadline);
    }
    if (!endDate) return 0;

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    if (today < startDate) return 0;
    if (today > endDate) return 100;

    const totalDays = Math.floor((endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24));
    const elapsedDays = Math.floor((today.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24));

    if (totalDays <= 0) return 0;
    return Math.min(100, (elapsedDays / totalDays) * 100);
  }

  /**
   * Calculate progress based on sprint completion
   */
  private calculateSprintsProgress(project: Project): number {
    if (!project || project.totalPlannedSprints <= 0) return 0;
    const completedSprints = project.completedSprints || 0;
    const progress = (completedSprints / project.totalPlannedSprints) * 100;
    return Math.min(100, isNaN(progress) ? 0 : progress);
  }

  /**
   * Calculate progress based on completed phases
   */
  private calculatePhasesProgress(project: Project): number {
    // If project has sprintTimeline data, use it to estimate phases
    if (!project.sprintTimeline || project.sprintTimeline.length === 0) {
      // Fallback: use sprints progress if no timeline available
      return this.calculateSprintsProgress(project);
    }
    
    const completedPhases = project.sprintTimeline.filter(s => s.status === 'completed').length;
    const totalPhases = project.sprintTimeline.length;
    if (totalPhases <= 0) return 0;
    return (completedPhases / totalPhases) * 100;
  }

  /**
   * Parse date string safely
   */
  private parseDate(dateString: string | undefined): Date | null {
    if (!dateString || dateString === '-') return null;
    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) return null;
      return date;
    } catch (e) {
      return null;
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