import { Component, OnInit, OnDestroy } from '@angular/core';
import { PeopleHealthService, WorkforceHealthSummary, HeadcountTrend, WatchlistEntry } from '../services/people-health.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-people-health-list',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  templateUrl: './people-health-list.component.html',
  styleUrls: ['./people-health-list.component.css']
})
export class PeopleHealthListComponent implements OnInit, OnDestroy {
  // Workforce health data
  workforceHealthSummary: WorkforceHealthSummary | null = null;
  headcountTrend: HeadcountTrend[] = [];
  watchlist: WatchlistEntry[] = [];

  // UI states
  loading = true;
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
   */
  loadWorkforceHealthData(): void {
    this.loading = true;
    this.error = null;

    // Fetch summary data
    this.peopleHealthService.getWorkforceHealthSummary()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (summary) => {
          this.workforceHealthSummary = summary;
          this.loading = false;
        },
        error: (err) => {
          console.error('Failed to load workforce health summary:', err);
          this.error = 'Failed to load workforce health data';
          this.workforceHealthSummary = {
            presentToday: null,
            onBreak: null,
            onLeave: null,
            lateArrivals: null,
            presentInOffice: null,
            attendanceConsistency: null
          };
          this.loading = false;
        }
      });

    // Fetch headcount trend data
    this.peopleHealthService.getHeadcountTrend()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (trends) => {
          this.headcountTrend = trends || [];
        },
        error: (err) => {
          console.error('Failed to load headcount trend:', err);
          this.headcountTrend = [];
        }
      });

    // Fetch watchlist data
    this.peopleHealthService.getWorkforceHealthWatchlist()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (watchlist) => {
          this.watchlist = watchlist || [];
        },
        error: (err) => {
          console.error('Failed to load watchlist:', err);
          this.watchlist = [];
        }
      });
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
