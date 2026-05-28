import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { environment } from 'src/environments/environment';

export interface WorkforceHealthSummary {
  presentToday: number | null;
  onBreak: number | null;
  onLeave: number | null;
  lateArrivals: number | null;
  presentInOffice: number | null;
  attendanceConsistency: number | null;
}

export interface HeadcountTrend {
  date: string;
  headcount: number;
}

export interface AttendanceLogEntry {
  id: number;
  employeeId: string;
  employeeName: string;
  department: string;
  checkInTime: string;
  checkOutTime: string;
  status: string;
  date: string;
}

export interface WatchlistEntry {
  id: number;
  employeeId: string;
  employeeName: string;
  reason: string;
  severity: string;
  date: string;
}

@Injectable({
  providedIn: 'root'
})
export class PeopleHealthService {
  private apiUrl = `${environment.apiUrl}/api/v3/hrms/workforce-health`;
  private workforceHealthSummary$ = new BehaviorSubject<WorkforceHealthSummary | null>(null);
  private headcountTrend$ = new BehaviorSubject<HeadcountTrend[] | null>(null);
  private loading$ = new BehaviorSubject<boolean>(false);
  private error$ = new BehaviorSubject<string | null>(null);

  constructor(private http: HttpClient) {}

  /**
   * Fetch workforce health summary from API
   */
  getWorkforceHealthSummary(): Observable<WorkforceHealthSummary> {
    return this.http.get<any>(`${this.apiUrl}/summary`).pipe(
      map(response => {
        const data = response?.data || {};
        const summary: WorkforceHealthSummary = {
          presentToday: data.presentToday ?? null,
          onBreak: data.onBreak ?? null,
          onLeave: data.onLeave ?? null,
          lateArrivals: data.lateArrivals ?? null,
          presentInOffice: data.presentInOffice ?? null,
          attendanceConsistency: data.attendanceConsistency ?? null
        };
        this.workforceHealthSummary$.next(summary);
        return summary;
      }),
      catchError(error => {
        console.error('Error fetching workforce health summary:', error);
        this.error$.next('Failed to fetch workforce health summary');
        throw error;
      })
    );
  }

  /**
   * Fetch headcount trend from API
   */
  getHeadcountTrend(): Observable<HeadcountTrend[]> {
    return this.http.get<any>(`${this.apiUrl}/headcount-trend`).pipe(
      map(response => {
        const trends: HeadcountTrend[] = response?.data || [];
        this.headcountTrend$.next(trends);
        return trends;
      }),
      catchError(error => {
        console.error('Error fetching headcount trend:', error);
        this.error$.next('Failed to fetch headcount trend');
        throw error;
      })
    );
  }

  /**
   * Fetch attendance log from API
   */
  getAttendanceLog(
    searchTerm?: string,
    departmentId?: number,
    fromDate?: string,
    toDate?: string,
    page: number = 0,
    size: number = 10
  ): Observable<any> {
    let url = `${this.apiUrl}/attendance-log?page=${page}&size=${size}`;
    if (searchTerm) url += `&searchTerm=${searchTerm}`;
    if (departmentId) url += `&departmentId=${departmentId}`;
    if (fromDate) url += `&fromDate=${fromDate}`;
    if (toDate) url += `&toDate=${toDate}`;

    return this.http.get<any>(url).pipe(
      map(response => response?.data || {}),
      catchError(error => {
        console.error('Error fetching attendance log:', error);
        this.error$.next('Failed to fetch attendance log');
        throw error;
      })
    );
  }

  /**
   * Fetch workforce health watchlist from API
   */
  getWorkforceHealthWatchlist(): Observable<WatchlistEntry[]> {
    return this.http.get<any>(`${this.apiUrl}/watchlist`).pipe(
      map(response => response?.data || []),
      catchError(error => {
        console.error('Error fetching workforce health watchlist:', error);
        this.error$.next('Failed to fetch watchlist');
        throw error;
      })
    );
  }

  /**
   * Get cached workforce health summary
   */
  getWorkforceHealthSummary$(): Observable<WorkforceHealthSummary | null> {
    return this.workforceHealthSummary$.asObservable();
  }

  /**
   * Get cached headcount trend
   */
  getHeadcountTrend$(): Observable<HeadcountTrend[] | null> {
    return this.headcountTrend$.asObservable();
  }

  /**
   * Get loading state
   */
  getLoading$(): Observable<boolean> {
    return this.loading$.asObservable();
  }

  /**
   * Get error state
   */
  getError$(): Observable<string | null> {
    return this.error$.asObservable();
  }

  /**
   * Reset error state
   */
  clearError(): void {
    this.error$.next(null);
  }
}
