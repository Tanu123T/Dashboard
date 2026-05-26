import { Injectable } from '@angular/core';
import { ApiService } from 'src/app/core/services/api.service';
import { Observable } from 'rxjs';
import { ApiResponse, Page, EmployeeProfileDTO } from '../models/employee.model';
import { HttpParams } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class EmployeeFeatureService {

  constructor(private api: ApiService) {}

  getEmployees(page: number = 0, size: number = 100): Observable<ApiResponse<Page<EmployeeProfileDTO>>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
      
    // Using HttpClient directly here since ApiService doesn't support params yet, 
    // or we can append it to the URL string. Let's append it to the URL string since ApiService only takes a string.
    return this.api.get(`/api/v1/hrms/employees?page=${page}&size=${size}`) as Observable<ApiResponse<Page<EmployeeProfileDTO>>>;
  }

  getEmployeeById(id: string): Observable<ApiResponse<EmployeeProfileDTO>> {
    return this.api.get(`/api/v1/hrms/employees/${id}`) as Observable<ApiResponse<EmployeeProfileDTO>>;
  }

  getEmployeeFull(id: string) {
    return this.api.get(`/api/v1/hrms/employees/${id}/full`);
  }
}
