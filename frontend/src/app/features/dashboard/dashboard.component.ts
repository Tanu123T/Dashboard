import { Component, OnInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { PeopleHealthService, WorkforceHealthSummary } from '../people-health/services/people-health.service';
import { LucideAngularModule } from 'lucide-angular';
import {
  Sparkles,
  RotateCw,
  Calendar,
  Users,
  User,
  Briefcase,
  FileText,
  Zap,
  Folder,
  MoreHorizontal,
  LayoutGrid,
  Heart,
  Gift
} from 'lucide-angular';

type AttendanceStatus = 'present' | 'late' | 'leave';

interface AttendanceSnapshot {
  present: number | null;
  absent: number | null;
  leave: number | null;
  late: number | null;
  onBreak: number | null;
  remoteActive: number | null;
}

interface AttendanceRow {
  id: string;
  name: string;
  initials: string;
  employeeCode: string;
  department: string;
  checkIn: string;
  checkOut: string;
  hours: string;
  status: AttendanceStatus;
}

interface RepeatedLateOrAbsentItem {
  name: string;
  department: string;
  issue: string;
}

@Component({
  selector: 'app-ceo-dashboard',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  @ViewChild('attendanceDateInput') attendanceDateInput?: ElementRef<HTMLInputElement>;
  
  isWorkforceHealthRoute = false;
  private destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private peopleHealthService: PeopleHealthService
  ) {}

  ngOnInit() {
    // Check if this route is 'workforce-health', if so show content, otherwise empty
    this.isWorkforceHealthRoute = this.route.snapshot.component === DashboardComponent && 
                                   this.route.snapshot.url.length > 0 &&
                                   this.route.snapshot.url[0].path === 'workforce-health';

    // Fetch real workforce health data from API
    if (this.isWorkforceHealthRoute) {
      this.loadWorkforceHealthData();
    }
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Load workforce health data from API
   */
  private loadWorkforceHealthData(): void {
    this.peopleHealthService.getWorkforceHealthSummary()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data: WorkforceHealthSummary) => {
          // Update attendance snapshot with real API data
          this.attendanceSnapshot = {
            present: data.presentToday ?? null,
            absent: null, // Not provided by API
            leave: data.onLeave ?? null,
            late: data.lateArrivals ?? null,
            onBreak: data.onBreak ?? null,
            remoteActive: null // Not provided by API
          };
          
          // Update summary cards with real data
          this.updateSummaryCards();
        },
        error: (error) => {
          console.error('Error loading workforce health data:', error);
          // Keep showing dashes for missing data
          this.attendanceSnapshot = {
            present: null,
            absent: null,
            leave: null,
            late: null,
            onBreak: null,
            remoteActive: null
          };
          this.updateSummaryCards();
        }
      });

    // Load attendance log data
    this.loadAttendanceLogData();

    // Load headcount trend data
    this.loadHeadcountTrendData();
  }

  /**
   * Update summary cards dynamically based on real API data
   */
  private updateSummaryCards(): void {
    this.summaryCards = [
      { label: 'Present Today', value: this.formatValue(this.attendanceSnapshot.present), subtitle: 'Checked in and active', icon: 'user-check', tone: 'green' },
      { label: 'On Break', value: this.formatValue(this.attendanceSnapshot.onBreak), subtitle: 'Temporarily unavailable', icon: 'coffee', tone: 'blue' },
      { label: 'On Leave', value: this.formatValue(this.attendanceSnapshot.leave), subtitle: 'Planned leaves in effect', icon: 'calendar', tone: 'amber' },
      { label: 'Late Arrivals', value: this.formatValue(this.attendanceSnapshot.late), subtitle: 'Past shift start threshold', icon: 'clock', tone: 'orange' },
      { label: 'Present in Office', value: this.formatValue(null), subtitle: 'On-site and active', icon: 'building', tone: 'mint' }, // Not in API yet
      { label: 'Attendance Consistency', value: this.formatPercentage(null), subtitle: 'Last 7 operational days', icon: 'users', tone: 'green' } // Not in API yet
    ];
  }

  /**
   * Format value or return dash if null
   */
  private formatValue(value: number | null): string {
    if (value === null || value === undefined) {
      return '-';
    }
    return String(value);
  }

  /**
   * Format percentage or return dash if null
   */
  private formatPercentage(value: number | null): string {
    if (value === null || value === undefined) {
      return '-';
    }
    return `${value.toFixed(1)}%`;
  }

  attendanceSnapshot: AttendanceSnapshot = {
    present: null,
    absent: null,
    leave: null,
    late: null,
    onBreak: null,
    remoteActive: null
  };

  summaryCards = [
    { label: 'Present Today', value: '-', subtitle: 'Checked in and active', icon: 'user-check', tone: 'green' },
    { label: 'On Break', value: '-', subtitle: 'Temporarily unavailable', icon: 'coffee', tone: 'blue' },
    { label: 'On Leave', value: '-', subtitle: 'Planned leaves in effect', icon: 'calendar', tone: 'amber' },
    { label: 'Late Arrivals', value: '-', subtitle: 'Past shift start threshold', icon: 'clock', tone: 'orange' },
    { label: 'Present in Office', value: '-', subtitle: 'On-site and active', icon: 'building', tone: 'mint' },
    { label: 'Attendance Consistency', value: '-', subtitle: 'Last 7 operational days', icon: 'users', tone: 'green' }
  ];

  iconStroke = 'currentColor';

  months: string[] = [];

  chartPoints: number[] = [];

  chartData: Array<{ month: string; actual: number; target: number }> = [];

  showTooltip = false;
  tooltipX = 0;
  tooltipY = 0;
  tooltipMonth = '';
  tooltipValue = 0;
  tooltipTarget = 0;
  hoverLineX = 0;

  selectedAttendanceDate = '2026-05-11';

  attendanceRows: AttendanceRow[] = [];

  repeatedLateOrAbsent: RepeatedLateOrAbsentItem[] = [];

  /**
   * Fetch attendance log data from API
   */
  private loadAttendanceLogData(): void {
    this.peopleHealthService.getAttendanceLog(undefined, undefined, undefined, undefined, 0, 100)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data: any) => {
          const content = data.content || [];
          // Map API response to attendance rows
          this.attendanceRows = content.map((log: any) => ({
            id: log.id || log.employeeId,
            name: log.employeeName || '-',
            initials: this.getInitials(log.employeeName || ''),
            employeeCode: log.employeeId || '-',
            department: log.department || '-',
            checkIn: log.checkInTime || '-',
            checkOut: log.checkOutTime || '-',
            hours: log.hours || '-',
            status: this.determineStatus(log.status) as AttendanceStatus
          }));
        },
        error: (error) => {
          console.error('Error loading attendance log:', error);
          this.attendanceRows = [];
        }
      });
  }

  /**
   * Fetch headcount trend data from API
   */
  private loadHeadcountTrendData(): void {
    this.peopleHealthService.getHeadcountTrend()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (trends: any[]) => {
          if (trends && trends.length > 0) {
            // Map trend data to chart format
            this.chartData = trends.map((trend: any) => ({
              month: this.formatMonthFromDate(trend.date),
              actual: trend.headcount || 0,
              target: 209 // Default target if not provided
            }));

            // Extract months
            this.months = this.chartData.map(d => d.month);

            // Extract chart points
            this.chartPoints = this.chartData.map(d => d.actual);
          }
        },
        error: (error) => {
          console.error('Error loading headcount trend:', error);
          this.chartData = [];
          this.months = [];
          this.chartPoints = [];
        }
      });
  }

  /**
   * Format date to month abbreviation
   */
  private formatMonthFromDate(dateString: string): string {
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString('en-US', { month: 'short' });
    } catch {
      return dateString;
    }
  }

  /**
   * Determine attendance status from API data
   */
  private determineStatus(status: string): AttendanceStatus {
    const statusLower = status?.toLowerCase() || '';
    if (statusLower.includes('late')) return 'late';
    if (statusLower.includes('leave') || statusLower.includes('absent')) return 'leave';
    return 'present';
  }

  get chartPath(): string {
    const width = 1000;
    const height = 220;
    const min = 0;
    const max = 100;

    if (this.chartPoints.length === 0) {
      return '';
    }

    const denominator = Math.max(1, this.chartPoints.length - 1);

    return this.chartPoints
      .map((value, index) => {
        const x = (index / denominator) * width;
        const y = height - ((value - min) / (max - min)) * 150 - 30;
        return `${index === 0 ? 'M' : 'L'} ${x} ${y}`;
      })
      .join(' ');
  }

  private generateChartPoints(dataKey: 'actual' | 'target'): Array<{ x: number; y: number }> {
    const width = 1000;
    const height = 220;
    const minVal = 0;
    const maxVal = 240;
    const chartHeight = 160;

    if (this.chartData.length === 0) {
      return [];
    }

    const denominator = Math.max(1, this.chartData.length - 1);

    return this.chartData.map((data, index) => {
      const x = (index / denominator) * width;
      const value = dataKey === 'actual' ? data.actual : data.target;
      const normalized = (value - minVal) / (maxVal - minVal);
      const y = height - (normalized * chartHeight) - 30;
      return { x, y };
    });
  }

  private generateSmoothPath(points: Array<{ x: number; y: number }>): string {
    if (points.length === 0) return '';

    if (points.length === 1) {
      return `M ${points[0].x} ${points[0].y}`;
    }

    let path = `M ${points[0].x} ${points[0].y}`;

    for (let i = 0; i < points.length - 1; i++) {
      const current = points[i];
      const next = points[i + 1];

      // Control points for smooth cubic bezier curve
      const cp1x = current.x + (next.x - current.x) * 0.33;
      const cp1y = current.y;
      const cp2x = next.x - (next.x - current.x) * 0.33;
      const cp2y = next.y;

      path += ` C ${cp1x} ${cp1y} ${cp2x} ${cp2y} ${next.x} ${next.y}`;
    }

    return path;
  }

  get actualLinePath(): string {
    const points = this.generateChartPoints('actual');
    return this.generateSmoothPath(points);
  }

  get targetLinePath(): string {
    const points = this.generateChartPoints('target');
    return this.generateSmoothPath(points);
  }

  get areaPath(): string {
    const points = this.generateChartPoints('actual');
    const height = 220;

    if (points.length === 0) {
      return '';
    }

    let path = this.generateSmoothPath(points);

    const firstX = points[0].x;
    const lastX = points[points.length - 1].x;

    // Close the area to the baseline.
    path += ` L ${lastX} ${height} L ${firstX} ${height} Z`;
    return path;
  }

  getInitials(name: string): string {
    return name
      .split(' ')
      .filter(Boolean)
      .slice(0, 2)
      .map(part => part.charAt(0))
      .join('')
      .toUpperCase();
  }

  getStatusLabel(status: AttendanceStatus): string {
    if (status === 'leave') {
      return 'On Leave';
    }

    return status.charAt(0).toUpperCase() + status.slice(1);
  }

  getStatusTone(status: AttendanceStatus): 'green' | 'amber' | 'slate' {
    if (status === 'late') {
      return 'amber';
    }

    if (status === 'leave') {
      return 'slate';
    }

    return 'green';
  }

  openAttendanceDatePicker(): void {
    const input = this.attendanceDateInput?.nativeElement;
    if (!input) {
      return;
    }

    if (typeof input.showPicker === 'function') {
      input.showPicker();
      return;
    }

    input.focus();
    input.click();
  }

  onAttendanceDateChange(value: string): void {
    this.selectedAttendanceDate = value;
  }

  get attendanceDateLabel(): string {
    const date = new Date(`${this.selectedAttendanceDate}T00:00:00`);
    if (Number.isNaN(date.getTime())) {
      return 'Select Date';
    }

    return new Intl.DateTimeFormat('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric'
    }).format(date);
  }

  trackByLabel(_: number, item: { label: string }): string {
    return item.label;
  }

  onChartHover(event: MouseEvent): void {
    if (!this.chartData || this.chartData.length === 0) {
      this.hideTooltip();
      return;
    }

    const chartArea = event.currentTarget as HTMLElement;
    const rect = chartArea.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;

    // Calculate which month is being hovered based on x position
    const relativeX = x / rect.width; // 0 to 1
    let monthIndex = Math.floor(relativeX * this.chartData.length);
    const clampedIndex = Math.max(0, Math.min(monthIndex, this.chartData.length - 1));

    const data = this.chartData[clampedIndex];
    if (!data) {
      this.hideTooltip();
      return;
    }
    this.tooltipMonth = data.month;
    this.tooltipValue = data.actual;
    this.tooltipTarget = data.target;

    // Calculate hover line X position
    // First, get the pixel-based center of the month column
    const monthPixelWidth = rect.width / this.chartData.length;
    const monthCenterPixel = (clampedIndex + 0.5) * monthPixelWidth;
    
    // Convert pixel position to SVG viewBox coordinates (0-1000)
    this.hoverLineX = (monthCenterPixel / rect.width) * 1000;

    // Position tooltip at the actual mouse position
    this.tooltipX = x - 90;
    this.tooltipY = y - 100;

    this.showTooltip = true;
  }

  private hideTooltip(): void {
    this.showTooltip = false;
    this.tooltipMonth = '';
    this.tooltipValue = 0;
    this.tooltipTarget = 0;
    this.hoverLineX = 0;
  }
}
