import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from 'src/environments/environment';

export interface ProjectStats {
  total: number;
  complete: number;
  inProgress: number;
  delayed: number;
}

export interface Project {
  id: number;
  name: string;
  client: string;
  status: string;
  statusLabel: string;
  progress: number;
  lead: string;
  startDate: string;
  dueDate: string;
  description: string;
  techStack: string[];
  team: string[];
  totalPlannedSprints: number;
  completedSprints: number;
  activeSprints: number;
  sprintTimeline?: Sprint[];
  icon?: string;
  iconClass?: string;
}

export interface Sprint {
  id: number;
  name: string;
  status: string;
  progress: number;
}

export interface ProjectsPageResponse {
  summary: {
    totalProjects: number;
    complete: number;
    inProgress: number;
    delayed: number;
  };
  projects: Project[];
}

@Injectable({
  providedIn: 'root'
})
export class ProjectsService {
  private apiUrl = `${environment.apiUrl}/projects`;
  private stats$ = new BehaviorSubject<ProjectStats>({
    total: 0,
    complete: 0,
    inProgress: 0,
    delayed: 0
  });

  constructor(private http: HttpClient) {}

  // Fetch all projects with stats
  getAllProjects(): Observable<Project[]> {
    return this.http.get<ProjectsPageResponse>(this.apiUrl).pipe(
      map(response => {
        const projects = response.projects.map(p => this.mapApiProjectToLocal(p));
        this.updateStats(response.summary);
        return projects;
      })
    );
  }

  // Get single project detail
  getProjectById(id: number): Observable<Project> {
    return this.http.get<Project>(`${this.apiUrl}/${id}`).pipe(
      map(p => this.mapApiProjectToLocal(p))
    );
  }

  // Get cached stats
  getStats(): ProjectStats {
    return this.stats$.value;
  }

  // Map API response to local interface
  private mapApiProjectToLocal(apiProject: any): Project {
    const rawStatus = (apiProject.status || '').toString().trim();
    const status = this.getStatusClass(rawStatus);
    const statusLabel = rawStatus || 'Unknown';

    return {
      id: apiProject.id,
      name: apiProject.name,
      client: apiProject.client,
      status: status as 'complete' | 'in-progress' | 'delayed',
      statusLabel: statusLabel,
      progress: apiProject.progress || 0,
      lead: apiProject.lead,
      startDate: this.formatDate(apiProject.startDate),
      dueDate: this.formatDate(apiProject.dueDate),
      description: apiProject.description || '',
      techStack: apiProject.techStack || [],
      team: apiProject.team || [],
      totalPlannedSprints: apiProject.totalPlannedSprints || 0,
      completedSprints: apiProject.completedSprints || 0,
      activeSprints: apiProject.activeSprints || 0,
      sprintTimeline: apiProject.sprintTimeline || [],
      icon: this.getProjectIcon(status),
      iconClass: this.getProjectIconClass(status)
    };
  }

  private getStatusClass(status: string): string {
    const normalized = status.toLowerCase().trim();

    // Handle OpenProject project statuses directly
    if (normalized === 'on track') return 'on-track';
    if (normalized === 'finished' || normalized === 'complete' || normalized === 'completed') return 'finished';
    if (normalized === 'at risk') return 'at-risk';
    if (normalized === 'off track') return 'off-track';
    if (normalized === 'not started') return 'not-started';
    if (normalized === 'not set' || normalized === 'discontinued' || !normalized) return 'unknown';
    
    return normalized || 'unknown';
  }

  private formatDate(date: string | Date | null | undefined): string {
    if (!date) return '';
    const d = new Date(date);
    return d.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }

  private getProjectIcon(status: string): string {
    if (status === 'finished') return '✓';
    if (status === 'on-track' || status === 'not-started') return '•';
    if (status === 'at-risk' || status === 'off-track') return '!';
    return '•';
  }

  private getProjectIconClass(status: string): string {
    if (status === 'finished') return 'fas fa-briefcase';
    if (status === 'on-track') return 'fas fa-briefcase';
    if (status === 'at-risk' || status === 'off-track') return 'fas fa-briefcase';
    if (status === 'not-started') return 'fas fa-briefcase';
    return 'fas fa-briefcase';
  }

  private updateStats(summary: any) {
    this.stats$.next({
      total: summary.totalProjects,
      complete: summary.complete,
      inProgress: summary.inProgress,
      delayed: summary.delayed
    });
  }
}
