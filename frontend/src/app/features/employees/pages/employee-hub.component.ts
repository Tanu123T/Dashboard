import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { EmployeeFeatureService } from '../services/employee-feature.service';
import { EmployeeProfileDTO } from '../models/employee.model';
import { Subject, timer, Subscription, of } from 'rxjs';
import { switchMap, catchError, takeUntil, finalize, retry } from 'rxjs/operators';

interface EmployeeCard {
  id: string;
  initials: string;
  name: string;
  title: string;
  status: 'Active' | 'Inactive' | 'On Leave';
  department: string;
  location: string;
  stability: number;
  color: string;
}

@Component({
  selector: 'app-employee-hub',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './employee-hub.component.html',
  styleUrls: ['./employee-hub.component.css']
})
export class EmployeeHubComponent implements OnInit, OnDestroy {
  searchQuery = '';
  showAllEmployees = false;
  employees: EmployeeCard[] = [];
  filteredEmployees: EmployeeCard[] = [];
  
  isLoading = true;
  errorMessage = '';

  private destroy$ = new Subject<void>();
  private pollingSubscription?: Subscription;

  // Pre-defined gradients for visual appeal
  private colorPalette = [
    'linear-gradient(135deg, #6f7ef7 0%, #5b6df0 45%, #7c8cff 100%)',
    'linear-gradient(135deg, #f7a34c 0%, #f89f35 45%, #fbb74f 100%)',
    'linear-gradient(135deg, #5aa7ff 0%, #4c8ef6 45%, #67d3d8 100%)',
    'linear-gradient(135deg, #7a6ee8 0%, #6d82f2 45%, #6bb1e5 100%)',
    'linear-gradient(135deg, #2ea79a 0%, #4ea7e1 45%, #73c7cf 100%)',
    'linear-gradient(135deg, #4a9df2 0%, #42b6da 45%, #4fd0c5 100%)'
  ];

  constructor(
    private router: Router,
    private employeeService: EmployeeFeatureService
  ) {}

  ngOnInit() {
    this.startDataPolling();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private startDataPolling() {
    // Poll every 60 seconds (60000 ms)
    this.pollingSubscription = timer(0, 60000).pipe(
      takeUntil(this.destroy$),
      switchMap(() => {
        // Fetch up to 100 employees for the hub display
        return this.employeeService.getEmployees(0, 100).pipe(
          retry(2), // Retry twice before failing
          catchError(error => {
            console.error('Failed to fetch employees', error);
            this.errorMessage = 'Unable to load employee data. Please try again later.';
            return of(null);
          })
        );
      })
    ).subscribe(response => {
      if (response && response.data && response.data.content) {
        this.employees = this.mapToCards(response.data.content);
        this.filterEmployees();
        this.errorMessage = '';
      }
      this.isLoading = false;
    });
  }

  private mapToCards(dtos: EmployeeProfileDTO[]): EmployeeCard[] {
    return dtos.map((dto, index) => {
      // Calculate initials safely
      const names = dto.fullName.split(' ').filter(n => n.length > 0);
      let initials = 'EMP';
      if (names.length === 1) {
        initials = names[0].substring(0, 2).toUpperCase();
      } else if (names.length >= 2) {
        initials = (names[0][0] + names[names.length - 1][0]).toUpperCase();
      }

      // Map backend status to UI status
      let uiStatus: 'Active' | 'Inactive' | 'On Leave' = 'Inactive';
      const beStatus = (dto.employeeStatus || '').toLowerCase();
      if (beStatus.includes('active')) uiStatus = 'Active';
      else if (beStatus.includes('leave')) uiStatus = 'On Leave';

      // Pick a color consistently based on ID or index
      const colorIndex = (dto.id || index) % this.colorPalette.length;

      return {
        id: dto.id.toString(),
        initials: initials,
        name: dto.fullName || 'Unknown Employee',
        title: dto.designation || 'Staff',
        status: uiStatus,
        department: dto.department || 'General',
        location: dto.workMode || 'Office',
        stability: 85 + (index % 10), // Placeholder logic for stability metric
        color: this.colorPalette[colorIndex]
      };
    });
  }

  filterEmployees() {
    const query = this.searchQuery.toLowerCase();
    const matchingEmployees = !query ? this.employees : this.employees.filter(emp =>
        emp.name.toLowerCase().includes(query) ||
        emp.title.toLowerCase().includes(query) ||
        emp.department.toLowerCase().includes(query) ||
        emp.location.toLowerCase().includes(query)
      );

    this.filteredEmployees = this.showAllEmployees || query ? matchingEmployees : matchingEmployees.slice(0, 6);
  }

  onSearchChange() {
    this.filterEmployees();
  }

  toggleShowAllEmployees() {
    this.showAllEmployees = !this.showAllEmployees;
    this.filterEmployees();
  }

  viewEmployeeDetail(employeeId: string) {
    this.router.navigate(['/dashboard/employee-detail', employeeId]);
  }

  getShowCount(): string {
    return `Complete member directory - ${this.filteredEmployees.length} of ${this.employees.length} shown`;
  }

  getViewAllLabel(): string {
    return this.showAllEmployees ? 'Show Less' : 'View All';
  }
}
