import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';

export interface Member {
  id: string;
  name: string;
  role: string;
}

@Injectable({
  providedIn: 'root'
})
export class EmployeeService {

  // Mock data - will be replaced with API call later
  private mockEmployees: Member[] = [
    { id: '1', name: 'Ravindra Kulkarni', role: 'HR Manager' },
    { id: '2', name: 'Deepak Desai', role: 'Project Manager' },
    { id: '3', name: 'Priya Sharma', role: 'Senior Developer' },
    { id: '4', name: 'Neha Verma', role: 'UI Designer' },
    { id: '5', name: 'Vikram Singh', role: 'Backend Developer' },
    { id: '6', name: 'Anjali Gupta', role: 'Frontend Developer' },
    { id: '7', name: 'Rohit Kapoor', role: 'Business Analyst' }
  ];

  constructor() { }

  /**
   * Get all employees
   * Currently returns mock data
   * TODO: Replace with actual API call when backend is ready
   * Example: return this.http.get<Member[]>('/api/employees');
   */
  getEmployees(): Observable<Member[]> {
    return of(this.mockEmployees);
  }

  /**
   * Get employee by ID
   * TODO: Replace with API call when ready
   */
  getEmployeeById(id: string): Observable<Member | undefined> {
    return of(this.mockEmployees.find(emp => emp.id === id));
  }
}
