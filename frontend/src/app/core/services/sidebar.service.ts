import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SidebarService {
  private sidebarVisibleSubject = new BehaviorSubject<boolean>(true);
  public sidebarVisible$: Observable<boolean> = this.sidebarVisibleSubject.asObservable();

  constructor() { }

  toggleSidebar(): void {
    this.sidebarVisibleSubject.next(!this.sidebarVisibleSubject.value);
  }

  closeSidebar(): void {
    this.sidebarVisibleSubject.next(false);
  }

  openSidebar(): void {
    this.sidebarVisibleSubject.next(true);
  }

  getSidebarVisibility(): boolean {
    return this.sidebarVisibleSubject.value;
  }
}
