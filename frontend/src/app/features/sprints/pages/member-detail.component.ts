import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { SprintFeatureService, Project, ProjectsResponse } from '../services/sprint-feature.service';
import { Subject, forkJoin, of } from 'rxjs';
import { takeUntil, map, catchError } from 'rxjs/operators';

interface MemberSprint {
  sprintId: number;
  sprintName: string;
  sprintStatus: string;
  storyPoints: number;
  bugsFixed: number;
  hours: number;
  taskCompletionPercentage: number;
}

interface MemberProfile {
  id: number;
  name: string;
  role: string;
  projectName: string;
  totalStories: number;
  bugsResolved: number;
  hoursWorked: number;
  sprintStats: MemberSprint[];
}

@Component({
  selector: 'app-member-detail',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './member-detail.component.html',
  styleUrls: ['./member-detail.component.css']
})
export class MemberDetailComponent implements OnInit, OnDestroy {
  memberName: string = '';
  projectId: number | null = null;
  selectedProjectId: number | null = null;
  selectedProject: Project | null = null;
  projects: Project[] = [];
  memberProjects: Project[] = [];
  memberProfile: MemberProfile | null = null;
  loading = true;
  loadingProjects = true;
  error: string | null = null;
  currentSprintIndex = 0;
  private destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private sprintService: SprintFeatureService
  ) {}

  ngOnInit() {
    this.route.params.pipe(takeUntil(this.destroy$)).subscribe(params => {
      this.memberName = params['member'];
      this.projectId = +params['projectId'];
      
      if (this.memberName && this.projectId) {
        this.selectedProjectId = this.projectId;
        this.loadProjects();
        this.loadMemberProfile();
      }
    });
  }

  loadProjects() {
    this.loadingProjects = true;
    this.sprintService.getAllProjects()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: ProjectsResponse) => {
          this.projects = response.projects;
          this.memberProjects = response.projects; // Initialize with all projects
          
          // Find selected project
          const found = this.projects.find(p => p.id === this.projectId);
          if (found) {
            this.selectedProject = found;
          }
          
          this.loadingProjects = false;
          // After projects are loaded, filter to only those with this member
          this.filterProjectsByMember();
        },
        error: (err: any) => {
          console.error('Error loading projects:', err);
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
      // Navigate to the new project's member detail
      this.router.navigate(['/dashboard/members', projectId, this.memberName]);
    }
  }

  private loadMemberProfile() {
    this.loading = true;
    this.error = null;

    this.sprintService.getMemberProfile(this.projectId!, this.memberName)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data: any) => {
          this.memberProfile = data;
          this.loading = false;
        },
        error: (err: any) => {
          console.error('Error loading member profile:', err);
          this.error = `Failed to load member profile: ${err.message}`;
          this.loading = false;
        }
      });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  goBack() {
    this.router.navigate(['/dashboard/sprints']);
  }

  previousSprint() {
    if (this.memberProfile && this.currentSprintIndex > 0) {
      this.currentSprintIndex--;
    }
  }

  nextSprint() {
    if (this.memberProfile && this.currentSprintIndex < this.memberProfile.sprintStats.length - 1) {
      this.currentSprintIndex++;
    }
  }

  getVisibleSprints() {
    if (!this.memberProfile) return [];
    const sprints = this.memberProfile.sprintStats;
    return sprints.slice(this.currentSprintIndex, this.currentSprintIndex + 3);
  }

  getStatusClass(status: string): string {
    return status?.toLowerCase() || 'planned';
  }

  getInitials(name: string): string {
    if (!name) return '';
    return name.split(' ').map(n => n[0]).join('').toUpperCase();
  }

  getSprintLabel(sprint: MemberSprint): string {
    // Format as "Sprint 8" or just the name if no ID
    if (sprint.sprintId && sprint.sprintId > 0) {
      return `Sprint ${sprint.sprintId}`;
    }
    return sprint.sprintName;
  }

  private filterProjectsByMember() {
    if (!this.memberName || !this.projects || this.projects.length === 0) {
      this.memberProjects = this.projects;
      return;
    }

    console.log(`Filtering projects for member: ${this.memberName}`);

    // Load team data for all projects to find which ones contain this member
    const projectTeamRequests = this.projects.map(project =>
      this.sprintService.getSprintsByProject(project.id).pipe(
        map((response: any) => {
          const teamMembers = (response && response.team) ? response.team.map((m: any) => m.name) : [];
          console.log(`Project ${project.name} (id=${project.id}): team members =`, teamMembers);
          return {
            projectId: project.id,
            projectName: project.name,
            teamMembers: teamMembers
          };
        }),
        catchError((err: any) => {
          console.warn(`Failed to load team for project ${project.id}:`, err);
          return of({
            projectId: project.id,
            projectName: project.name,
            teamMembers: []
          });
        })
      )
    );

    if (projectTeamRequests.length === 0) {
      this.memberProjects = this.projects;
      return;
    }

    forkJoin(projectTeamRequests)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (results: any[]) => {
          console.log('All project team data loaded:', results);
          
          // Filter projects to only those containing the current member
          const filteredProjects = results.filter(result => {
            const hasMember = result.teamMembers.some((tm: string) => 
              tm.toLowerCase() === this.memberName.toLowerCase()
            );
            console.log(`Project ${result.projectName}: has member ${this.memberName}? ${hasMember}`);
            return hasMember;
          });

          console.log('Filtered projects:', filteredProjects);

          if (filteredProjects.length > 0) {
            const filteredIds = filteredProjects.map(p => p.projectId);
            this.memberProjects = this.projects.filter(p => filteredIds.includes(p.id));
            console.log('Final memberProjects:', this.memberProjects);
          } else {
            console.log('Member not found in any project, showing all');
            this.memberProjects = this.projects;
          }
        },
        error: (err: any) => {
          console.error('Error loading team data:', err);
          this.memberProjects = this.projects;
        }
      });
  }
}
