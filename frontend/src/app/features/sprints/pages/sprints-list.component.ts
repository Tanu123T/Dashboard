import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SprintFeatureService, Project, ProjectsResponse } from '../services/sprint-feature.service';
import { Sprint, SprintStats, TeamMember } from '../models/sprint.model';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Router } from '@angular/router';

@Component({
  selector: 'app-sprints-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './sprints-list.component.html',
  styleUrls: ['./sprints-list.component.css']
})
export class SprintsListComponent implements OnInit, OnDestroy {
  sprints: Sprint[] = [];
  filteredSprints: Sprint[] = [];
  sprintFilter: 'all' | 'active' | 'completed' = 'all';
  teamMembers: TeamMember[] = [];
  projects: Project[] = [];
  selectedProjectId: number | null = null;
  selectedProject: Project | null = null;
  stats: SprintStats = {
    totalPlanned: 0,
    completed: 0,
    active: 0,
    avgCompletion: 0,
    teamSize: 0
  };
  loading = true;
  loadingProjects = true;
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
    this.loadProjects();
  }

  loadProjects() {
    this.loadingProjects = true;
    this.error = null;
    
    this.sprintService.getAllProjects()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: ProjectsResponse) => {
          this.projects = response.projects;
          this.loadingProjects = false;
          
          // Auto-select first project if available
          if (this.projects.length > 0) {
            this.selectedProjectId = this.projects[0].id;
            this.selectedProject = this.projects[0];
            this.loadSprintsByProject();
          }
        },
        error: (err: any) => {
          console.error('Error loading projects:', err);
          this.error = `Failed to load projects: ${err.status === 0 ? 'Backend server is not running' : err.message}`;
          this.loadingProjects = false;
        }
      });
  }

  onProjectChange() {
    if (this.selectedProjectId) {
      const projectId = Number(this.selectedProjectId);
      const found = this.projects.find(p => p.id === projectId);
      if (found) {
        this.selectedProject = found;
      }
      this.loadSprintsByProject();
    }
  }

  loadSprintsByProject() {
    if (!this.selectedProjectId) {
      return;
    }

    this.loading = true;
    this.error = null;
    
    const projectId = Number(this.selectedProjectId);
    
    // Fetch sprint dashboard data from API for selected project
    this.sprintService.getSprintsByProject(projectId)
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
              totalTasks: sprint.totalTasks || 0,
              storyPoints: sprint.storyPoints || 0,
              bugsFix: sprint.bugsFix || 0,
              hours: sprint.hours || '0h'
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

          this.filterSprints();
          this.loading = false;
        },
        error: (err: any) => {
          console.error('Error loading sprint data:', err);
          this.error = `Failed to load sprint data: ${err.status === 0 ? 'Backend server is not running' : err.message}`;
          this.loading = false;
        }
      });
  }

  filterSprints() {
    if (this.sprintFilter === 'all') {
      this.filteredSprints = this.sprints;
    } else if (this.sprintFilter === 'active') {
      this.filteredSprints = this.sprints.filter(s => s.status?.toLowerCase() === 'active');
    } else if (this.sprintFilter === 'completed') {
      this.filteredSprints = this.sprints.filter(s => s.status?.toLowerCase() === 'completed');
    }
  }

  setSprintFilter(filter: 'all' | 'active' | 'completed') {
    this.sprintFilter = filter;
    this.filterSprints();
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
    // Light blue color for all team member avatars
    return '#3b82f6';
  }

  getStatusClass(status: string): string {
    return status === 'completed' ? 'completed' : status === 'active' ? 'active' : 'planned';
  }

  getInitials(name: string): string {
    return name.split(' ').map(n => n[0]).join('').toUpperCase();
  }

  openSprintDetail(sprintId: number) {
    this.router.navigate(['/dashboard/sprints', sprintId], {
      state: { projectName: this.selectedProject?.name }
    });
  }

  openMemberProfile(memberName: string) {
    if (this.selectedProjectId) {
      const projectId = Number(this.selectedProjectId);
      this.router.navigate(['/dashboard/members', projectId, memberName]);
    } else {
      console.warn('No project selected');
    }
  }
}
