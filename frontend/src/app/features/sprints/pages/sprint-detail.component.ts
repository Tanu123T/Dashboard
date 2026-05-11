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

  burndown = [
    { day: 'D1', ideal: 45, actual: 45 },
    { day: 'D2', ideal: 38, actual: 42 },
    { day: 'D3', ideal: 31, actual: 36 },
    { day: 'D4', ideal: 24, actual: 30 },
    { day: 'D5', ideal: 17, actual: 22 },
    { day: 'D6', ideal: 10, actual: 14 },
    { day: 'D7', ideal: 3, actual: 6 }
  ];

  sprintAlerts = [
    { title: 'Scope change detected', detail: '2 new work items added after sprint start', tone: 'warning' },
    { title: 'Burndown lagging', detail: 'Actual trend is 4 points above ideal', tone: 'danger' },
    { title: 'Team capacity stable', detail: 'No blockers reported in last 24 hours', tone: 'success' }
  ];

  kpis = [
    { label: 'Sprint Points', value: '45', tone: 'info', icon: 'chart' },
    { label: 'Completed', value: '31', tone: 'success', icon: 'check' },
    { label: 'Remaining', value: '14', tone: 'warning', icon: 'clock' },
    { label: 'Blocked', value: '2', tone: 'danger', icon: 'alert' }
  ];

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

  get burndownIdealPath(): string {
    return this.buildLinePath('ideal');
  }

  get burndownActualPath(): string {
    return this.buildLinePath('actual');
  }

  get burndownAreaPath(): string {
    return this.buildAreaPath('actual');
  }

  get burndownMax(): number {
    return 50;
  }

  private buildLinePath(key: 'ideal' | 'actual'): string {
    const width = 1000;
    const height = 320;
    const paddingTop = 24;
    const paddingBottom = 44;
    const usableHeight = height - paddingTop - paddingBottom;

    return this.burndown
      .map((point, index) => {
        const x = (index / (this.burndown.length - 1)) * width;
        const value = point[key];
        const y = paddingTop + (usableHeight - ((value / this.burndownMax) * usableHeight));
        return `${index === 0 ? 'M' : 'L'} ${x} ${y}`;
      })
      .join(' ');
  }

  private buildAreaPath(key: 'actual'): string {
    const width = 1000;
    const height = 320;
    const paddingTop = 24;
    const paddingBottom = 44;
    const usableHeight = height - paddingTop - paddingBottom;

    const line = this.burndown
      .map((point, index) => {
        const x = (index / (this.burndown.length - 1)) * width;
        const value = point[key];
        const y = paddingTop + (usableHeight - ((value / this.burndownMax) * usableHeight));
        return `${index === 0 ? 'M' : 'L'} ${x} ${y}`;
      })
      .join(' ');

    return `${line} L 1000 276 L 0 276 Z`;
  }
}
