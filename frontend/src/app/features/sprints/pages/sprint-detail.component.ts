import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { SprintFeatureService } from '../services/sprint-feature.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

interface BurndownPoint {
  day: string;
  ideal: number;
  actual: number;
}

interface SprintKpi {
  label: string;
  value: string;
  tone: 'info' | 'success' | 'warning' | 'danger';
  icon: 'chart' | 'check' | 'clock' | 'alert';
}

interface SprintAlert {
  title: string;
  detail: string;
  tone: 'warning' | 'danger' | 'success';
}

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

  burndownPoints: BurndownPoint[] = [];
  sprintAlerts: SprintAlert[] = [];
  kpis: SprintKpi[] = [];

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
          this.burndownPoints = this.mapBurndownPoints(data?.burndownChart);
          this.kpis = this.buildKpis(data);

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

  get sprintMasterLabel(): string {
    return this.sprintDetail?.scrumMaster || this.sprintDetail?.sprintMaster || '-';
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
    return this.burndownPoints.reduce((max, point) => Math.max(max, point.ideal, point.actual), 0);
  }

  private buildLinePath(key: 'ideal' | 'actual'): string {
    if (!this.burndownPoints.length || this.burndownMax <= 0) {
      return '';
    }

    const width = 1000;
    const height = 320;
    const paddingTop = 24;
    const paddingBottom = 44;
    const usableHeight = height - paddingTop - paddingBottom;

    return this.burndownPoints
      .map((point, index) => {
        const x = this.burndownPoints.length === 1
          ? width / 2
          : (index / (this.burndownPoints.length - 1)) * width;
        const value = point[key];
        const y = paddingTop + (usableHeight - ((value / this.burndownMax) * usableHeight));
        return `${index === 0 ? 'M' : 'L'} ${x} ${y}`;
      })
      .join(' ');
  }

  private buildAreaPath(key: 'actual'): string {
    const line = this.buildLinePath(key);
    if (!line) {
      return '';
    }

    return `${line} L 1000 276 L 0 276 Z`;
  }

  private mapBurndownPoints(chartData: any): BurndownPoint[] {
    if (!Array.isArray(chartData)) {
      return [];
    }

    return chartData
      .map((point: any) => ({
        day: String(point?.day ?? '').trim(),
        ideal: this.toNumber(point?.ideal),
        actual: this.toNumber(point?.actual)
      }))
      .filter(point => point.day.length > 0);
  }

  private buildKpis(data: any): SprintKpi[] {
    const totalTasks = this.toNumber(data?.totalTasks);
    const completedTasks = this.toNumber(data?.completedTasks);
    const hasRemainingData = data?.todoTasks !== undefined || data?.inProgressTasks !== undefined || data?.testingTasks !== undefined;
    const remainingTasks = [data?.todoTasks, data?.inProgressTasks, data?.testingTasks]
      .reduce((sum, value) => sum + this.toNumber(value), 0);

    return [
      { label: 'Sprint Points', value: this.formatMetric(data?.storyPoints), tone: 'info', icon: 'chart' },
      { label: 'Completed', value: this.formatMetric(data?.completedTasks), tone: 'success', icon: 'check' },
      { label: 'Remaining', value: hasRemainingData ? String(remainingTasks) : (data?.totalTasks != null && data?.completedTasks != null ? String(Math.max(totalTasks - completedTasks, 0)) : ''), tone: 'warning', icon: 'clock' },
      { label: 'Blocked', value: this.formatMetric(data?.blockedTasks ?? data?.blocked), tone: 'danger', icon: 'alert' }
    ];
  }

  private buildAlerts(_: any): SprintAlert[] {
    return [];
  }

  private formatMetric(value: any): string {
    return value === null || value === undefined ? '' : String(value);
  }

  private toNumber(value: any): number {
    const number = Number(value);
    return Number.isFinite(number) ? number : 0;
  }
}
