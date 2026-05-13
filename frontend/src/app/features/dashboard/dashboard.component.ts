import { Component, ElementRef, ViewChild, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';

type AttendanceStatus = 'present' | 'late' | 'leave';

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

interface AttendanceSnapshot {
  present: number;
  absent: number;
  leave: number;
  late: number;
  onBreak: number;
  remoteActive: number;
}

interface RepeatedLateOrAbsentItem {
  name: string;
  department: string;
  issue: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  @ViewChild('attendanceDateInput') attendanceDateInput?: ElementRef<HTMLInputElement>;
  
  isWorkforceHealthRoute = false;

  constructor(private route: ActivatedRoute) {}

  ngOnInit() {
    // Check if this route is 'workforce-health', if so show content, otherwise empty
    this.isWorkforceHealthRoute = this.route.snapshot.component === DashboardComponent && 
                                   this.route.snapshot.url.length > 0 &&
                                   this.route.snapshot.url[0].path === 'workforce-health';
  }

  attendanceSnapshot: AttendanceSnapshot = {
    present: 228,
    absent: 9,
    leave: 7,
    late: 14,
    onBreak: 11,
    remoteActive: 62
  };

  summaryCards = [
    { label: 'Present Today', value: String(this.attendanceSnapshot.present), subtitle: 'Checked in and active', icon: 'user-check', tone: 'green' },
    { label: 'On Break', value: String(this.attendanceSnapshot.onBreak), subtitle: 'Temporarily unavailable', icon: 'coffee', tone: 'blue' },
    { label: 'On Leave', value: String(this.attendanceSnapshot.leave), subtitle: 'Planned leaves in effect', icon: 'calendar', tone: 'amber' },
    { label: 'Late Arrivals', value: String(this.attendanceSnapshot.late), subtitle: 'Past shift start threshold', icon: 'clock', tone: 'orange' },
    { label: 'Present in Office', value: '166', subtitle: 'On-site and active', icon: 'building', tone: 'mint' },
    { label: 'Attendance Consistency', value: '94%', subtitle: 'Last 7 operational days', icon: 'users', tone: 'green' }
  ];

  iconStroke = 'currentColor';

  months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'];

  chartPoints = [52, 53, 54, 55, 56, 57];

  selectedAttendanceDate = '2026-05-11';

  attendanceRows: AttendanceRow[] = [
    { id: 'E-001', name: 'Sarah Chen', initials: 'SC', employeeCode: 'E001', department: 'Engineering', checkIn: '08:31', checkOut: '', hours: '', status: 'present' },
    { id: 'E-002', name: 'James Wilson', initials: 'JW', employeeCode: 'E002', department: 'Sales', checkIn: '08:48', checkOut: '', hours: '', status: 'present' },
    { id: 'E-003', name: 'Priya Patel', initials: 'PP', employeeCode: 'E003', department: 'Product', checkIn: '09:05', checkOut: '', hours: '', status: 'present' },
    { id: 'E-004', name: 'Marcus Lee', initials: 'ML', employeeCode: 'E004', department: 'Marketing', checkIn: '09:22', checkOut: '', hours: '', status: 'late' },
    { id: 'E-005', name: 'Elena Torres', initials: 'ET', employeeCode: 'E005', department: 'Design', checkIn: '--', checkOut: '', hours: '', status: 'leave' },
    { id: 'E-006', name: 'Alex Kim', initials: 'AK', employeeCode: 'E006', department: 'Engineering', checkIn: '09:56', checkOut: '', hours: '', status: 'late' },
    { id: 'E-007', name: 'Anna Kowalski', initials: 'AK', employeeCode: 'E007', department: 'HR', checkIn: '10:13', checkOut: '', hours: '', status: 'late' },
    { id: 'E-008', name: 'Li Wei', initials: 'LW', employeeCode: 'E008', department: 'Engineering', checkIn: '08:25', checkOut: '', hours: '', status: 'present' },
    { id: 'E-009', name: 'Ryan O Brien', initials: 'RB', employeeCode: 'E009', department: 'Sales', checkIn: '08:42', checkOut: '', hours: '', status: 'present' },
    { id: 'E-010', name: 'Sofia Garcia', initials: 'SG', employeeCode: 'E010', department: 'Marketing', checkIn: '08:59', checkOut: '', hours: '', status: 'present' },
    { id: 'E-011', name: 'Maya Singh', initials: 'MS', employeeCode: 'E011', department: 'Design', checkIn: '09:16', checkOut: '', hours: '', status: 'present' },
    { id: 'E-012', name: 'David Brown', initials: 'DB', employeeCode: 'E012', department: 'Finance', checkIn: '09:33', checkOut: '', hours: '', status: 'late' }
  ];

  repeatedLateOrAbsent: RepeatedLateOrAbsentItem[] = [
    {
      name: 'Ryan O Brien',
      department: 'Sales',
      issue: '3 late check-ins this week'
    },
    {
      name: 'Maya Singh',
      department: 'Design',
      issue: '2 absences in 10 days'
    },
    {
      name: 'Sofia Garcia',
      department: 'Marketing',
      issue: 'Extended leave overlap with campaign sprint'
    }
  ];

  get chartPath(): string {
    const width = 1000;
    const height = 220;
    const min = 0;
    const max = 100;

    return this.chartPoints
      .map((value, index) => {
        const x = (index / (this.chartPoints.length - 1)) * width;
        const y = height - ((value - min) / (max - min)) * 150 - 30;
        return `${index === 0 ? 'M' : 'L'} ${x} ${y}`;
      })
      .join(' ');
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
}
