import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EmployeeService, Member } from '../../../../core/services/employee.service';

interface Hierarchy {
  ceo: { name: string; role: string };
  tierTwo: Member[];
  tierThree: Member[];
}

@Component({
  selector: 'app-org-hierarchy',
  templateUrl: './org-hierarchy.component.html',
  styleUrls: ['./org-hierarchy.component.css'],
  standalone: true,
  imports: [CommonModule]
})
export class OrgHierarchyComponent implements OnInit {
  isFullView = false;
  lastUpdated = '';
  hierarchy: Hierarchy = {
    ceo: { name: 'Rajendra Gangarde', role: 'Chief Executive Officer' },
    tierTwo: [],
    tierThree: []
  };
  members: Member[] = [];

  constructor(private employeeService: EmployeeService) {}

  ngOnInit() {
    this.loadEmployees();
    this.setLastUpdated();
  }

  /**
   * Load employees from service
   * When API is integrated, this will automatically fetch from the backend
   */
  private loadEmployees() {
    this.employeeService.getEmployees().subscribe(
      (employees: Member[]) => {
        this.members = employees;
        this.buildHierarchy();
      },
      (error: unknown) => {
        console.error('Error loading employees:', error);
      }
    );
  }

  private setLastUpdated() {
    const now = new Date();
    const options: Intl.DateTimeFormatOptions = {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      hour12: true
    };
    this.lastUpdated = now.toLocaleDateString('en-US', options);
  }

  private sortByName(a: Member, b: Member): number {
    return a.name.localeCompare(b.name);
  }

  buildHierarchy() {
    const tierTwo = this.members
      .filter((member) => {
        const role = member.role.toLowerCase();
        return (
          role.includes('hr') ||
          role.includes('manager') ||
          role.includes('director') ||
          role.includes('lead')
        );
      })
      .sort((a, b) => {
        // HR Manager should come first (higher priority)
        if (a.role.toLowerCase().includes('hr')) return -1;
        if (b.role.toLowerCase().includes('hr')) return 1;
        return this.sortByName(a, b);
      });

    const tierTwoIds = new Set(tierTwo.map((member) => member.id));
    const tierThree = this.members
      .filter((member) => !tierTwoIds.has(member.id))
      .sort((a, b) => this.sortByName(a, b));

    this.hierarchy = {
      ceo: { name: 'Rajendra Gangarde', role: 'Chief Executive Officer' },
      tierTwo,
      tierThree
    };
  }

  toggleView() {
    this.isFullView = !this.isFullView;
  }

  handleOrgChartWheel(event: WheelEvent) {
    const target = event.currentTarget as HTMLElement;
    const shouldScrollHorizontally =
      Math.abs(event.deltaY) > Math.abs(event.deltaX);

    if (!shouldScrollHorizontally) {
      return;
    }

    target.scrollLeft += event.deltaY;
    event.preventDefault();
  }
}
