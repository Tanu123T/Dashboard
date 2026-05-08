import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { SprintFeatureService } from '../services/sprint-feature.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-sprint-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './sprint-detail.component.html',
  styleUrls: ['./sprint-detail.component.css']
})
export class SprintDetailComponent implements OnInit, OnDestroy {
  sprintId: number | null = null;
  sprintDetail: any = null;
  projectName: string = 'Sprints';
  loading = true;
  error: string | null = null;
  private destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private sprintService: SprintFeatureService
  ) {
    // Get project name from navigation state (passed from sprints-list)
    const historyState = (this.router.getCurrentNavigation()?.extras as any)?.state;
    if (historyState?.projectName) {
      this.projectName = historyState.projectName;
    }
  }

  ngOnInit() {
    // Also try to get projectName from history state as fallback
    if (!this.projectName || this.projectName === 'Sprints') {
      const state = (window.history.state as any);
      if (state?.projectName) {
        this.projectName = state.projectName;
      }
    }

    this.route.params.pipe(takeUntil(this.destroy$)).subscribe(params => {
      this.sprintId = +params['id'];
      if (this.sprintId) {
        this.loadSprintDetail(this.sprintId);
      }
    });
  }

  private loadSprintDetail(id: number) {
    this.loading = true;
    this.sprintService.getSprintDetail(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data: any) => {
          this.sprintDetail = data;
          // If projectName still not set and API response contains it, use it
          if ((!this.projectName || this.projectName === 'Sprints') && data?.projectName) {
            this.projectName = data.projectName;
          }
          this.loading = false;
        },
        error: (err: any) => {
          console.error('Error loading sprint detail:', err);
          this.error = 'Failed to load sprint details';
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
}
