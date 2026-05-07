import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { SidebarService } from '../../core/services/sidebar.service';

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
  executionMenuOpen = true;
  peopleHealthMenuOpen = true;
  activeTab: string = 'overview';
  sidebarVisible: boolean = true;

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

  constructor(private router: Router, private sidebarService: SidebarService) {}

  ngOnInit() {
    this.updateActiveTab();
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        this.updateActiveTab();
      });
    
    this.sidebarService.sidebarVisible$.subscribe(visible => {
      this.sidebarVisible = visible;
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

  activateOverviewTab(event: Event) {
    event.preventDefault();
    event.stopPropagation();
    this.activeTab = 'overview';
    this.router.navigate([this.overviewSubMenus[0].route]);
  }

  activateExecutionTab(event: Event) {
    event.preventDefault();
    event.stopPropagation();
    this.activeTab = 'execution';
    this.router.navigate([this.executionSubMenus[0].route]);
  }

  activatePeopleHealthTab(event: Event) {
    event.preventDefault();
    event.stopPropagation();
    this.activeTab = 'people';
    this.router.navigate([this.peopleHealthSubMenus[0].route]);
  }

  toggleOverviewMenu(event: Event) {
    event.preventDefault();
    event.stopPropagation();
    this.overviewMenuOpen = !this.overviewMenuOpen;
  }

  toggleExecutionMenu(event: Event) {
    event.preventDefault();
    event.stopPropagation();
    this.executionMenuOpen = !this.executionMenuOpen;
  }

  togglePeopleHealthMenu(event: Event) {
    event.preventDefault();
    event.stopPropagation();
    this.peopleHealthMenuOpen = !this.peopleHealthMenuOpen;
  }

  handleLogout() {
    alert('Successfully logged out!');
    // Add logout logic here
  }

  handleSettings() {
    // Add settings navigation here
  }
}

