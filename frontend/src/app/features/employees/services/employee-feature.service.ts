import { Injectable } from '@angular/core';
import { ApiService } from 'src/app/core/services/api.service';
import { of } from 'rxjs';

@Injectable()
export class EmployeeFeatureService {

  constructor(private api: ApiService) {}

  getEmployees() {

    // 🧪 DUMMY DATA FIRST
    return of([
      { id: 1, name: 'Rahul', role: 'Developer' },
      { id: 2, name: 'Sneha', role: 'Tester' }
    ]);

    // REAL API LATER
    // return this.api.get('/employees');
  }

  addEmployee(emp: any) {
    return this.api.post('/employees', emp);
  }
}