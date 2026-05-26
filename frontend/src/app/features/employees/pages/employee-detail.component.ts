import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { EmployeeFeatureService } from '../services/employee-feature.service';
import { Subject, of } from 'rxjs';
import { takeUntil, catchError, finalize } from 'rxjs/operators';

interface PersonalInfo {
  email: string;
  phone: string;
  department: string;
  location: string;
  joinDate: string;
  manager: string;
}

interface PerformanceTrend {
  month: string;
  value: number;
}

interface Project {
  name: string;
  status: 'In Progress' | 'Completed' | 'On Hold';
  stages: string[];
  progress?: number;
}

interface WorkExperience {
  title: string;
  company: string;
  duration: string;
  description: string;
  years: string;
}

interface Education {
  degree: string;
  institution: string;
  field: string;
  year: string;
}

interface Achievement {
  title: string;
  date: string;
}

interface DetailItem {
  label: string;
  value: string;
}

interface EmployeeDetail {
  id: string;
  name: string;
  title: string;
  department: string;
  email: string;
  phone: string;
  photo?: string;
  initials: string;
  color: string;
  personalInfo: PersonalInfo;
  attendance: {
    totalDays: number;
    presentDays: number;
    absentDays: number;
    attendanceRate: number;
  };
  performanceTrends: PerformanceTrend[];
  skills: string[];
  projects: Project[];
  workExperience: WorkExperience[];
  education: Education[];
  achievements: Achievement[];
  detailItems: DetailItem[];
}

@Component({
  selector: 'app-employee-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './employee-detail.component.html',
  styleUrls: ['./employee-detail.component.css']
})
export class EmployeeDetailComponent implements OnInit, OnDestroy {
  employee: EmployeeDetail | null = null;
  loading = true;
  error: string | null = null;
  private destroy$ = new Subject<void>();

  private colorPalette = [
    'linear-gradient(135deg, #6f7ef7 0%, #5b6df0 45%, #7c8cff 100%)',
    'linear-gradient(135deg, #f7a34c 0%, #f89f35 45%, #fbb74f 100%)',
    'linear-gradient(135deg, #5aa7ff 0%, #4c8ef6 45%, #67d3d8 100%)',
    'linear-gradient(135deg, #7a6ee8 0%, #6d82f2 45%, #6bb1e5 100%)',
    'linear-gradient(135deg, #2ea79a 0%, #4ea7e1 45%, #73c7cf 100%)',
    'linear-gradient(135deg, #4a9df2 0%, #42b6da 45%, #4fd0c5 100%)'
  ];

  constructor(
    private route: ActivatedRoute,
    private employeeService: EmployeeFeatureService
  ) {}

  ngOnInit() {
    this.route.params.pipe(takeUntil(this.destroy$)).subscribe(params => {
      const employeeId = params['id'];
      if (employeeId) {
        this.loadEmployeeDetail(employeeId);
      }
    });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadEmployeeDetail(employeeId: string) {
    this.loading = true;
    this.error = null;
    this.employeeService.getEmployeeFull(employeeId).pipe(
      takeUntil(this.destroy$),
      catchError(err => {
        console.error('Failed to load enriched employee details', err);
        this.error = 'Failed to load employee profile.';
        return of(null);
      }),
      finalize(() => this.loading = false)
    ).subscribe((response: any) => {
      if (response && response.data) {
        const payload = response.data;
        const emp = payload.employee || {};
        const edu = payload.education || [];
        const work = payload.workExperience || [];
        const perf = payload.performanceTrends || [];
        const projects = payload.projects || [];
        const attendance = payload.attendance || { totalDays: 0, presentDays: 0, absentDays: 0, attendanceRate: 0 };
        const derivedSkills = Array.isArray(payload.skills) ? payload.skills : [];
        const achievements = Array.isArray(payload.achievements) ? payload.achievements : [];

        const fullName = this.resolveFullName(emp, employeeId);
        const department = emp.department || 'General';
        const designation = emp.designation || 'Staff';
        const location = emp.location || (emp.branchId ? `Branch ${emp.branchId}` : department);

        this.employee = {
          id: String(emp.id || employeeId),
          name: fullName,
          title: `${designation} | ${department}`,
          department: department,
          email: emp.officialEmail || 'N/A',
          phone: 'N/A',
          photo: emp.profileImage || undefined,
          initials: this.getInitials(fullName),
          color: this.colorPalette[(emp.id || 0) % this.colorPalette.length],
          personalInfo: {
            email: emp.officialEmail || 'N/A',
            phone: 'N/A',
            department: department,
            location: location,
            joinDate: this.formatDate(emp.employmentDate),
            manager: emp.reportingManagerName || 'N/A'
          },
          attendance: {
            totalDays: Number(attendance.totalDays || 0),
            presentDays: Number(attendance.presentDays || 0),
            absentDays: Number(attendance.absentDays || Math.max(0, Number(attendance.totalDays || 0) - Number(attendance.presentDays || 0))),
            attendanceRate: Number(attendance.attendanceRate || 0)
          },
          performanceTrends: perf.map((p: any) => ({ month: p.month, value: Math.round(Number(p.avg_value ?? p.avgValue ?? p.value) || 0) })),
          skills: this.mergeSkills(derivedSkills, projects),
          projects: projects.map((p: any) => ({
            name: p.name || 'Unnamed Project',
            status: this.resolveProjectStatus(p.status),
            stages: this.resolveTechStack(p.techStack || p.tech_stack || p.tech_stack_csv),
            progress: Number(p.progress || 0)
          })),
          workExperience: work.map((w: any) => ({
            title: w.title || w.job_title || 'Role',
            company: w.companyName || w.company_name || w.company || '',
            duration: this.formatRange(w.startDate || w.start_date, w.endDate || w.end_date),
            description: w.description || '',
            years: ''
          })),
          education: edu.map((e: any) => ({
            degree: e.description || 'Education',
            institution: e.institution || '',
            field: e.grade || '',
            year: this.formatRange(e.start_year || e.startYear, e.end_date || e.endDate)
          })),
          achievements: achievements.map((a: any) => ({
            title: a.title || a.name || '',
            date: a.date || a.period || ''
          })),
          detailItems: this.normalizeDetailItems(payload.detailItems || [])
        };
      }
    });
  }

  getInitials(fullName: string): string {
    const names = fullName.split(' ').filter(n => n.length > 0);
    if (names.length === 0) return '??';
    if (names.length === 1) return names[0].substring(0, 2).toUpperCase();
    return (names[0][0] + names[names.length - 1][0]).toUpperCase();
  }

  getAttendanceLabel(rate: number): string {
    if (rate >= 90) return 'Excellent';
    if (rate >= 80) return 'Good';
    if (rate >= 70) return 'Average';
    return 'Needs Improvement';
  }

  getActiveProjectsCount(): number {
    if (!this.employee) return 0;
    return this.employee.projects.filter(p => p.status === 'In Progress').length;
  }

  getPerformancePath(): string {
    const employee = this.employee;
    if (!employee || !employee.performanceTrends.length) {
      return 'M 0 160 L 80 140 L 160 122 L 240 110 L 320 102 L 400 96 L 480 92';
    }

    const points = employee.performanceTrends.map((point, index) => ({
      x: 24 + (index * 430 / Math.max(1, employee.performanceTrends.length - 1)),
      y: 170 - Math.min(140, Math.max(10, point.value * 1.1))
    }));

    return points.map((point, index) => `${index === 0 ? 'M' : 'L'} ${point.x.toFixed(0)} ${point.y.toFixed(0)}`).join(' ');
  }

  getPerformanceAreaPath(): string {
    const line = this.getPerformancePath();
    if (!line.startsWith('M')) {
      return line;
    }

    const endX = 24 + (Math.max(0, (this.employee?.performanceTrends.length || 1) - 1) * 430 / Math.max(1, (this.employee?.performanceTrends.length || 1) - 1));
    return `${line} L ${endX.toFixed(0)} 190 L 24 190 Z`;
  }

  private resolveFullName(emp: any, fallbackId: string): string {
    const candidate = [emp.fullName, emp.full_name, emp.firstName, emp.first_name, emp.lastName, emp.last_name]
      .filter((value: string | undefined | null) => !!value)
      .join(' ')
      .trim();

    if (candidate) {
      return candidate;
    }

    return emp.employeeCode || emp.employee_code || fallbackId || 'Unknown';
  }

  private resolveTechStack(techStack: any): string[] {
    if (Array.isArray(techStack)) {
      return techStack.filter(item => !!item).map((item: any) => String(item));
    }

    if (typeof techStack === 'string') {
      return techStack.split(',').map(item => item.trim()).filter(Boolean);
    }

    return [];
  }

  private resolveProjectStatus(status: any): 'In Progress' | 'Completed' | 'On Hold' {
    const normalized = String(status || '').toLowerCase();
    if (normalized.includes('hold')) return 'On Hold';
    if (normalized.includes('progress') || normalized.includes('active')) return 'In Progress';
    return 'Completed';
  }

  private formatDate(value: any): string {
    if (!value) return 'N/A';
    const text = String(value);
    return text.includes('T') ? text.split('T')[0] : text;
  }

  private formatRange(startValue: any, endValue: any): string {
    const startText = this.formatDate(startValue);
    const endText = this.formatDate(endValue);
    return `${startText} - ${endText}`;
  }

  private mergeSkills(skills: string[], projects: any[]): string[] {
    const merged = new Set<string>(skills.map(skill => skill.trim()).filter(Boolean));
    projects.forEach(project => {
      this.resolveTechStack(project.techStack || project.tech_stack || project.tech_stack_csv).forEach(skill => merged.add(skill));
    });
    return Array.from(merged);
  }

  private normalizeDetailItems(items: any[]): DetailItem[] {
    if (!Array.isArray(items)) return [];

    return items.map(item => ({
      label: String(item?.label || ''),
      value: this.formatDetailValue(item?.value)
    })).filter(item => item.label.length > 0);
  }

  private formatDetailValue(value: any): string {
    if (value === null || value === undefined || value === '') return 'N/A';
    if (typeof value === 'number') return Number.isFinite(value) ? String(value) : 'N/A';
    return String(value);
  }
}

