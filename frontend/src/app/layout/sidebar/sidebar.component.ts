import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';

interface MenuItem {
  label: string;
  route: string;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit {
  overviewMenuOpen = true;
  executionMenuOpen = false;
  peopleHealthMenuOpen = false;
  activeTab: string = 'overview';

  overviewSubMenus: MenuItem[] = [
    { label: 'Dashboard', route: '/dashboard' }
  ];

  executionSubMenus: MenuItem[] = [
    { label: 'Projects', route: '/dashboard/projects' },
    { label: 'Sprints', route: '/dashboard/sprints' }
  ];

  peopleHealthSubMenus: MenuItem[] = [
    { label: 'Workforce Health', route: '/dashboard/workforce-health' },
    { label: 'Employee Hub', route: '/dashboard/employee-hub' },
    { label: 'Work Calendar', route: '/dashboard/work-calendar' },
    { label: 'Org Hierarchy', route: '/dashboard/org-hierarchy' }
  ];

  constructor(private router: Router) {}

  ngOnInit() {
    this.updateActiveTab();
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        this.updateActiveTab();
      });
  }

  updateActiveTab() {
    const url = this.router.url;
    if (url.includes('/dashboard')) {
      if (url.includes('/projects') || url.includes('/sprints')) {
        this.activeTab = 'execution';
      } else if (
        url.includes('/workforce-health') ||
        url.includes('/employee-hub') ||
        url.includes('/work-calendar') ||
        url.includes('/org-hierarchy')
      ) {
        this.activeTab = 'people';
      } else {
        this.activeTab = 'overview';
      }
    }
  }

  isTabActive(tab: string): boolean {
    return this.activeTab === tab;
  }

  toggleOverviewMenu(event: Event) {
    event.preventDefault();
    event.stopPropagation();
    this.overviewMenuOpen = true;
    this.executionMenuOpen = false;
    this.peopleHealthMenuOpen = false;
    this.activeTab = 'overview';
  }

  toggleExecutionMenu(event: Event) {
    event.preventDefault();
    event.stopPropagation();
    this.overviewMenuOpen = false;
    this.executionMenuOpen = true;
    this.peopleHealthMenuOpen = false;
    this.activeTab = 'execution';
  }

  togglePeopleHealthMenu(event: Event) {
    event.preventDefault();
    event.stopPropagation();
    this.overviewMenuOpen = false;
    this.executionMenuOpen = false;
    this.peopleHealthMenuOpen = true;
    this.activeTab = 'people';
  }

  handleLogout() {
    alert('Successfully logged out!');
    // Add logout logic here
  }

  handleSettings() {
    // Add settings navigation here
  }
}

