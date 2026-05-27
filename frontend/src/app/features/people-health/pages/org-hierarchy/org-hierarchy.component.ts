import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

interface Member {
  id: string;
  name: string;
  role: string;
}

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
  hierarchy: Hierarchy = {
    ceo: { name: 'Rajendra Gangarde', role: 'Chief Executive Officer' },
    tierTwo: [],
    tierThree: []
  };
  members: Member[] = [
    { id: '1', name: 'Ravindra Kulkarni', role: 'HR Manager' },
    { id: '2', name: 'Deepak Desai', role: 'Project Manager' },
    { id: '3', name: 'Employee 1', role: 'Staff' },
    { id: '4', name: 'Employee 2', role: 'Staff' },
    { id: '5', name: 'Employee 3', role: 'Staff' }
  ];

  ngOnInit() {
    this.buildHierarchy();
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
      .sort((a, b) => this.sortByName(a, b));

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
