import { Injectable } from '@angular/core';
import { ApiService } from 'src/app/core/services/api.service';

@Injectable()
export class EmployeeFeatureService {

  constructor(private api: ApiService) {}

  getEmployees() {
    return this.api.get('/employees');
  }

  addEmployee(emp: any) {
    return this.api.post('/employees', emp);
  }
}