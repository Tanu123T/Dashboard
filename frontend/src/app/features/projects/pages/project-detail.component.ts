import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ProjectsService, Project, ProjectStats } from '../../../services/projects.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule } from '@angular/forms';

interface SprintRoadmapDot {
  id: number;
  status: 'completed' | 'active' | 'planned';
}

@Component({
  selector: 'app-project-detail',
  standalone: true,
  imports: [CommonModule, MatIconModule, FormsModule],
  templateUrl: './project-detail.component.html',
  styleUrls: ['./project-detail.component.css']
})
export class ProjectDetailComponent implements OnInit, OnDestroy {
  project: Project | null = null;
  pageLastUpdated: string = '';
  loading = true;
  error: string | null = null;
  developmentStartDate: string = '';
  endDate: string = '-';
  deadline: string = '-';
  sprintDuration: string = '';
  projectStatus: string = '';
  editingField: string | null = null;
  tempDate: string = '';
  tempValue: string = '';
  
  // Lifecycle phases - hardcoded as before
  lifecyclePhases: Array<{
    id: number;
    name: string;
    milestone: string;
    status: string;
    statusClass: string;
    startDate: string;
    targetCompletion: string;
  }> = [
    {
      id: 1,
      name: 'Phase 1',
      milestone: 'Project Initiation & Requirement Engineering',
      status: 'Completed',
      statusClass: 'completed',
      startDate: '01 Jan 2026',
      targetCompletion: '20 Jan 2026'
    },
    {
      id: 2,
      name: 'Phase 2',
      milestone: 'Product Development & Sprint Execution',
      status: 'In Progress',
      statusClass: 'in-progress',
      startDate: '21 Jan 2026',
      targetCompletion: '30 Apr 2026'
    },
    {
      id: 3,
      name: 'Phase 3',
      milestone: 'Release Stabilization & Production Rollout',
      status: 'Pending',
      statusClass: 'pending',
      startDate: '01 May 2026',
      targetCompletion: '20 May 2026'
    }
  ];

  sprintRoadmapDots: SprintRoadmapDot[] = [];
  completedSprints = 0;
  activeSprintNumber: number | null = null;
  calculatedProgress: number = 0;

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
            this.initializeFromProjectData();
            this.calculateProjectProgress();
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

  /**
   * Initialize all component properties from real project data
   * Phases remain hardcoded, but dates and status are real
   */
  initializeFromProjectData() {
    if (!this.project) return;

    // Initialize milestone dates from project data
    this.developmentStartDate = this.project.startDate || '';
    this.deadline = this.project.dueDate || this.project.deadline || '';
    this.endDate = this.project.endDate || '';
    
    // Initialize project status based on status label
    this.projectStatus = this.project.statusLabel || 'Unknown';
    
    // Initialize sprint duration - calculate from completed and total sprints
    if (this.project.totalPlannedSprints > 0) {
      const sprintDays = this.calculateAverageSprintDuration();
      this.sprintDuration = sprintDays > 0 ? `${sprintDays} days` : 'Not set';
    } else {
      this.sprintDuration = 'Not set';
    }
  }

  /**
   * Calculate average sprint duration based on project dates
   */
  private calculateAverageSprintDuration(): number {
    if (!this.project || this.project.totalPlannedSprints <= 0) return 0;

    try {
      const startDate = this.parseDate(this.project.startDate);
      const endDate = this.parseDate(this.project.dueDate || this.project.deadline);
      
      if (!startDate || !endDate) return 0;

      const totalDays = Math.floor((endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24));
      if (totalDays <= 0) return 0;

      const sprintDays = Math.round(totalDays / this.project.totalPlannedSprints);
      return sprintDays;
    } catch (e) {
      console.warn('Error calculating sprint duration:', e);
      return 0;
    }
  }

  /**
   * Generate lifecycle phases from actual sprint timeline data
   * Each phase represents the completion of sprints in a logical group
   */
  private generateLifecyclePhasesFromSprints() {
    if (!this.project || !this.project.sprintTimeline || this.project.sprintTimeline.length === 0) {
      // If no sprint timeline, create phases based on total sprints
      this.lifecyclePhases = this.generateDefaultPhases();
      return;
    }

    // Group sprints into phases based on status
    const completedSprints = this.project.sprintTimeline.filter(s => s.status === 'completed' || s.status === 'Completed').length;
    const totalSprints = this.project.sprintTimeline.length;
    const sprintDuration = this.calculateAverageSprintDuration();

    this.lifecyclePhases = [];
    let phaseId = 1;
    let currentSprintCount = 0;

    // Phase 1: Completed sprints
    if (completedSprints > 0) {
      const startDate = this.project.startDate;
      const endDate = this.calculateDateAfterDays(startDate, completedSprints * sprintDuration);
      this.lifecyclePhases.push({
        id: phaseId++,
        name: `Phase 1`,
        milestone: `Sprints 1-${completedSprints} Completed`,
        status: 'Completed',
        statusClass: 'completed',
        startDate: startDate,
        targetCompletion: endDate
      });
      currentSprintCount = completedSprints;
    }

    // Phase 2: In-Progress sprints
    const inProgressSprints = Math.min(this.project.activeSprints, totalSprints - completedSprints);
    if (inProgressSprints > 0) {
      const startDate = this.calculateDateAfterDays(this.project.startDate, currentSprintCount * sprintDuration);
      const endDate = this.calculateDateAfterDays(startDate, inProgressSprints * sprintDuration);
      const phaseSprintStart = currentSprintCount + 1;
      const phaseSprintEnd = currentSprintCount + inProgressSprints;
      
      this.lifecyclePhases.push({
        id: phaseId++,
        name: `Phase ${phaseId - 1}`,
        milestone: `Sprints ${phaseSprintStart}-${phaseSprintEnd} In Progress`,
        status: 'In Progress',
        statusClass: 'in-progress',
        startDate: startDate,
        targetCompletion: endDate
      });
      currentSprintCount += inProgressSprints;
    }

    // Phase 3: Pending/Future sprints
    const pendingSprints = totalSprints - currentSprintCount;
    if (pendingSprints > 0) {
      const startDate = this.calculateDateAfterDays(this.project.startDate, currentSprintCount * sprintDuration);
      const endDate = this.calculateDateAfterDays(startDate, pendingSprints * sprintDuration);
      const phaseSprintStart = currentSprintCount + 1;
      const phaseSprintEnd = totalSprints;
      
      this.lifecyclePhases.push({
        id: phaseId++,
        name: `Phase ${phaseId - 1}`,
        milestone: `Sprints ${phaseSprintStart}-${phaseSprintEnd} Pending`,
        status: 'Pending',
        statusClass: 'pending',
        startDate: startDate,
        targetCompletion: endDate
      });
    }
  }

  /**
   * Generate default phases when sprint timeline data is not available
   * Based on total planned sprints
   */
  private generateDefaultPhases() {
    const phases = [];
    const total = this.project?.totalPlannedSprints || 3;
    const completed = this.project?.completedSprints || 0;
    const sprintDuration = this.calculateAverageSprintDuration();

    // Phase 1: Completed
    if (completed > 0) {
      const startDate = this.project?.startDate || '';
      const endDate = this.calculateDateAfterDays(startDate, completed * sprintDuration);
      phases.push({
        id: 1,
        name: 'Phase 1',
        milestone: `${completed} Sprints Completed`,
        status: 'Completed',
        statusClass: 'completed',
        startDate: startDate,
        targetCompletion: endDate
      });
    }

    // Phase 2: In Progress
    const active = this.project?.activeSprints || 0;
    if (active > 0) {
      const startDate = this.calculateDateAfterDays(this.project?.startDate || '', completed * sprintDuration);
      const endDate = this.calculateDateAfterDays(startDate, active * sprintDuration);
      phases.push({
        id: 2,
        name: 'Phase 2',
        milestone: `${active} Sprints In Progress`,
        status: 'In Progress',
        statusClass: 'in-progress',
        startDate: startDate,
        targetCompletion: endDate
      });
    }

    // Phase 3: Pending
    const pending = total - completed - active;
    if (pending > 0) {
      const startDate = this.calculateDateAfterDays(this.project?.startDate || '', (completed + active) * sprintDuration);
      const endDate = this.calculateDateAfterDays(startDate, pending * sprintDuration);
      phases.push({
        id: 3,
        name: 'Phase 3',
        milestone: `${pending} Sprints Pending`,
        status: 'Pending',
        statusClass: 'pending',
        startDate: startDate,
        targetCompletion: endDate
      });
    }

    return phases;
  }

  /**
   * Calculate a future date by adding days to a start date
   */
  private calculateDateAfterDays(startDateStr: string, days: number): string {
    try {
      const startDate = this.parseDate(startDateStr);
      if (!startDate) return '';
      
      const futureDate = new Date(startDate);
      futureDate.setDate(futureDate.getDate() + days);
      
      return this.formatDate(futureDate.toISOString().split('T')[0]);
    } catch (e) {
      return '';
    }
  }

  /**
   * Calculates project progress based on multiple factors:
   * 1. Days elapsed vs total project duration
   * 2. Sprints completed vs total planned sprints
   * 3. Lifecycle phases completed vs total phases
   */
  calculateProjectProgress() {
    if (!this.project) {
      this.calculatedProgress = 0;
      return;
    }

    // 1. Calculate days-based progress
    const daysProgress = this.calculateDaysProgress();

    // 2. Calculate sprints-based progress
    const sprintsProgress = this.calculateSprintsProgress();

    // 3. Calculate lifecycle phases progress
    const phasesProgress = this.calculateLifecycleProgress();

    // Combine all three metrics with equal weighting (33% each)
    this.calculatedProgress = Math.round((daysProgress + sprintsProgress + phasesProgress) / 3);
    
    // Ensure progress is between 0 and 100
    this.calculatedProgress = Math.max(0, Math.min(100, this.calculatedProgress));
  }

  /**
   * Safely parse a date string and validate it
   */
  private parseDate(dateString: string | undefined): Date | null {
    if (!dateString || dateString === '-') return null;
    
    try {
      const date = new Date(dateString);
      // Check if date is valid
      if (isNaN(date.getTime())) return null;
      return date;
    } catch (e) {
      console.warn('Invalid date string:', dateString);
      return null;
    }
  }

  /**
   * Calculate progress based on calendar days elapsed
   * From project start date to today, divided by total project duration
   */
  private calculateDaysProgress(): number {
    if (!this.project) return 0;

    // Parse start date (required field)
    const startDate = this.parseDate(this.project.startDate);
    if (!startDate) return 0;

    // Try to get end date from multiple sources
    let endDate = this.parseDate(this.project.dueDate);
    if (!endDate) {
      endDate = this.parseDate(this.project.deadline);
    }
    if (!endDate) {
      endDate = this.parseDate(this.endDate);
    }
    
    // If we still don't have an end date, can't calculate progress
    if (!endDate) return 0;

    const today = new Date();
    today.setHours(0, 0, 0, 0); // Reset time to start of day for accurate comparison

    // If project hasn't started yet
    if (today < startDate) return 0;

    // If project is overdue
    if (today > endDate) return 100;

    const totalDays = Math.floor((endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24));
    const elapsedDays = Math.floor((today.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24));

    if (totalDays <= 0) return 0;

    const progress = (elapsedDays / totalDays) * 100;
    return isNaN(progress) ? 0 : progress;
  }

  /**
   * Calculate progress based on sprint completion
   * Completed sprints / Total planned sprints
   */
  private calculateSprintsProgress(): number {
    if (!this.project || this.project.totalPlannedSprints <= 0) return 0;

    const completedSprints = this.project.completedSprints || 0;
    const totalSprints = this.project.totalPlannedSprints;

    if (totalSprints <= 0) return 0;

    const progress = (completedSprints / totalSprints) * 100;
    return isNaN(progress) ? 0 : Math.min(progress, 100);
  }

  /**
   * Calculate progress based on lifecycle phase completion
   * Completed phases / Total phases
   */
  private calculateLifecycleProgress(): number {
    const completedPhases = this.lifecyclePhases.filter(phase => phase.status === 'Completed').length;
    const totalPhases = this.lifecyclePhases.length;

    if (totalPhases <= 0) return 0;

    const progress = (completedPhases / totalPhases) * 100;
    return isNaN(progress) ? 0 : progress;
  }

  calculateSprintTimeline() {
    if (!this.project) return;
    const total = this.project.totalPlannedSprints;
    
    this.completedSprints = this.project.completedSprints;
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

  editDate(fieldName: string) {
    this.editingField = fieldName;
    this.tempDate = '';
  }

  saveDate() {
    if (this.tempDate && this.editingField) {
      const formattedDate = this.formatDate(this.tempDate);
      if (this.editingField === 'developmentStart') {
        this.developmentStartDate = formattedDate;
      } else if (this.editingField === 'endDate') {
        this.endDate = formattedDate;
      } else if (this.editingField === 'deadline') {
        this.deadline = formattedDate;
      } else if (this.editingField.startsWith('phase-')) {
        const parts = this.editingField.split('-');
        const phaseId = parseInt(parts[1], 10);
        const dateType = parts[2]; // 'start' or 'target'
        const phase = this.lifecyclePhases.find(p => p.id === phaseId);
        if (phase) {
          if (dateType === 'start') {
            phase.startDate = formattedDate;
          } else if (dateType === 'target') {
            phase.targetCompletion = formattedDate;
          }
        }
      }
      this.editingField = null;
      this.tempDate = '';
    }
  }

  cancelEdit() {
    this.editingField = null;
    this.tempDate = '';
  }

  formatDate(dateString: string): string {
    if (!dateString) return '-';
    
    try {
      const date = new Date(dateString);
      const options: Intl.DateTimeFormatOptions = { day: 'numeric', month: 'short', year: 'numeric' };
      return date.toLocaleDateString('en-US', options);
    } catch (e) {
      return dateString;
    }
  }

  editStatus(phaseId: number) {
    this.editingField = `phase-${phaseId}-status`;
  }

  changeStatus(phaseId: number, newStatus: string) {
    const phase = this.lifecyclePhases.find(p => p.id === phaseId);
    if (phase) {
      phase.status = newStatus;
      const statusMap: { [key: string]: string } = {
        'Completed': 'completed',
        'In Progress': 'in-progress',
        'Pending': 'pending'
      };
      phase.statusClass = statusMap[newStatus] || 'pending';
      this.editingField = null;
    }
  }

  getStatusOptions(): string[] {
    return ['Completed', 'In Progress', 'Pending'];
  }

  onStatusChange(phaseId: number, event: any) {
    const value = event.target?.value;
    if (value) {
      this.changeStatus(phaseId, value);
    }
  }

  editSprintDuration() {
    this.editingField = 'sprintDuration';
    this.tempValue = this.sprintDuration;
  }

  saveSprintDuration() {
    if (this.tempValue && this.editingField === 'sprintDuration') {
      this.sprintDuration = this.tempValue;
      this.editingField = null;
      this.tempValue = '';
    }
  }

  cancelSprintEdit() {
    this.editingField = null;
    this.tempValue = '';
  }
}
