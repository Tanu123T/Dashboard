import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Sprint, SprintStats, TeamMember } from '../models/sprint.model';

export interface Project {
  id: number;
  name: string;
}

export interface ProjectsResponse {
  summary: any;
  projects: Project[];
}

@Injectable({
  providedIn: 'root'
})
export class SprintFeatureService {
  private apiUrl = 'http://localhost:8081/sprints';
  private projectsApiUrl = 'http://localhost:8081/projects';

  constructor(private http: HttpClient) { }

  // Fetch all projects for dropdown
  getAllProjects(): Observable<ProjectsResponse> {
    return this.http.get<ProjectsResponse>(this.projectsApiUrl);
  }

  // Fetch sprints for a specific project from backend API
  getSprintsByProject(projectId: number): Observable<any> {
    // Call the backend API to get sprint dashboard which includes all sprints
    return this.http.get<any>(`${this.apiUrl}/project/${projectId}`);
  }

  // Get sprint detail from backend API
  getSprintDetail(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  // Get sprint stats from backend API
  getSprintStats(projectId: number): Observable<SprintStats> {
    return this.http.get<SprintStats>(`${this.apiUrl}/project/${projectId}/stats`);
  }

  // Get team members from backend API
  getTeamMembers(projectId: number): Observable<TeamMember[]> {
    return this.http.get<TeamMember[]>(`${this.apiUrl}/project/${projectId}/team`);
  }

  // Get member performance profile
  getMemberProfile(projectId: number, memberName: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/project/${projectId}/member/${memberName}`);
  }
}
