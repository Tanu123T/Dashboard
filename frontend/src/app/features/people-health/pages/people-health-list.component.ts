import { Component, OnInit, OnDestroy } from '@angular/core';
import { PeopleHealthService, WorkforceHealthSummary, HeadcountTrend, WatchlistEntry, AttendanceLogEntry } from '../services/people-health.service';
import { Subject, forkJoin } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { CommonModule } from '@angular/common';
import { MatIcon } from '@angular/material/icon';

@Component({
  selector: 'app-people-health-list',
  standalone: true,
  imports: [CommonModule, MatIcon],
  templateUrl: './people-health-list.component.html',
  styleUrls: ['./people-health-list.component.css']
})
export class PeopleHealthListComponent implements OnInit, OnDestroy {
  // Workforce health data
  workforceHealthSummary: WorkforceHealthSummary | null = null;
  headcountTrend: HeadcountTrend[] = [];
  watchlist: WatchlistEntry[] = [];
  attendanceLog: AttendanceLogEntry[] = [];

  // UI states
  loading = true;
  summaryLoading = false;
  trendLoading = false;
  attendanceLoading = false;
  watchlistLoading = false;
  error: string | null = null;
  activeTab = 'overview'; // overview, attendance, watchlist

  private destroy$ = new Subject<void>();

  constructor(private peopleHealthService: PeopleHealthService) {}

  ngOnInit(): void {
    this.loadWorkforceHealthData();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Load all workforce health data from API
   * Uses forkJoin to coordinate all parallel requests
   */
  loadWorkforceHealthData(): void {
    this.loading = true;
    this.error = null;
    this.summaryLoading = true;
    this.trendLoading = true;
    this.attendanceLoading = true;
    this.watchlistLoading = true;

    // Coordinate all API calls with forkJoin
    forkJoin({
      summary: this.peopleHealthService.getWorkforceHealthSummary(),
      trends: this.peopleHealthService.getHeadcountTrend(),
      attendance: this.peopleHealthService.getAttendanceLog(),
      watchlist: this.peopleHealthService.getWorkforceHealthWatchlist()
    })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (result) => {
          // Set all data
          this.workforceHealthSummary = result.summary;
          this.headcountTrend = result.trends || [];
          this.attendanceLog = result.attendance?.content || [];
          this.watchlist = result.watchlist || [];

          // Update individual loading states
          this.summaryLoading = false;
          this.trendLoading = false;
          this.attendanceLoading = false;
          this.watchlistLoading = false;
          this.loading = false;
          this.error = null;
        },
        error: (err) => {
          console.error('Failed to load workforce health data:', err);
          this.error = 'Failed to load workforce health data. Please try again.';
          
          // Reset loading states and data on error
          this.workforceHealthSummary = null;
          this.headcountTrend = [];
          this.attendanceLog = [];
          this.watchlist = [];
          
          this.summaryLoading = false;
          this.trendLoading = false;
          this.attendanceLoading = false;
          this.watchlistLoading = false;
          this.loading = false;
        }
      });
  }

  /**
   * Reload all data (for retry button)
   */
  reloadData(): void {
    this.loadWorkforceHealthData();
  }

  /**
   * Generate SVG path for headcount trend chart
   */
  generateChartPath(): string {
    if (!this.headcountTrend || this.headcountTrend.length === 0) {
      return '';
    }

    const width = 540;
    const height = 200;
    const minHeadcount = Math.min(...this.headcountTrend.map(t => t.headcount));
    const maxHeadcount = Math.max(...this.headcountTrend.map(t => t.headcount));
    const range = maxHeadcount - minHeadcount || 1;

    const points = this.headcountTrend.map((trend, index) => {
      const x = 40 + (index / (this.headcountTrend.length - 1 || 1)) * width;
      const y = 250 - ((trend.headcount - minHeadcount) / range) * height;
      return `${x},${y}`;
    });

    return points.join(' L');
  }

  /**
   * Get minimum headcount value
   */
  getMinHeadcount(): number {
    if (!this.headcountTrend || this.headcountTrend.length === 0) return 0;
    return Math.min(...this.headcountTrend.map(t => t.headcount));
  }

  /**
   * Get maximum headcount value
   */
  getMaxHeadcount(): number {
    if (!this.headcountTrend || this.headcountTrend.length === 0) return 0;
    return Math.max(...this.headcountTrend.map(t => t.headcount));
  }

  /**
   * Format value or return dash if null
   */
  formatValue(value: number | null): string {
    if (value === null || value === undefined) {
      return '-';
    }
    return value.toString();
  }

  /**
   * Format percentage value
   */
  formatPercentage(value: number | null): string {
    if (value === null || value === undefined) {
      return '-';
    }
    return `${value.toFixed(1)}%`;
  }

  /**
   * Switch active tab
   */
  switchTab(tab: string): void {
    this.activeTab = tab;
  }

  /**
   * Reload data
   */
  reloadData(): void {
    this.loadWorkforceHealthData();
  }
}
