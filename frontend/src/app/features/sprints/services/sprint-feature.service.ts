import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Sprint, SprintStats, TeamMember } from '../models/sprint.model';

@Injectable({
  providedIn: 'root'
})
export class SprintFeatureService {
  private apiUrl = 'http://localhost:8081/sprints';

  constructor(private http: HttpClient) { }

  // Fetch all sprints from backend API
  getAllSprints(): Observable<any> {
    // Call the backend API to get sprint dashboard which includes all sprints
    return this.http.get<any>(`${this.apiUrl}/project/1`);
  }

  // Get sprint detail from backend API
  getSprintDetail(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  // Get sprint stats from backend API
  getSprintStats(): Observable<SprintStats> {
    return this.http.get<SprintStats>(`${this.apiUrl}/project/1/stats`);
  }

  // Get team members from backend API
  getTeamMembers(): Observable<TeamMember[]> {
    return this.http.get<TeamMember[]>(`${this.apiUrl}/project/1/team`);
  }

  // Get all sprints by project
  getSprintsByProject(projectId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/project/${projectId}/all`);
  }

  // Get team member profile
  getTeamMemberProfile(projectId: number, memberName: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/project/${projectId}/member/${memberName}`);
  }
}
