import { Injectable } from "@angular/core";
import { ApiService } from "src/app/core/services/api.service";
import { Observable } from "rxjs";
import {
  ApiResponse,
  Page,
  EmployeeProfileDTO,
  EmployeeDashboardResponse,
  EmployeeHubPageDTO,
} from "../models/employee.model";

@Injectable({ providedIn: "root" })
export class EmployeeFeatureService {
  constructor(private api: ApiService) {}

  /** Legacy list — still used as fallback */
  getEmployees(
    page = 0,
    size = 100,
  ): Observable<ApiResponse<Page<EmployeeProfileDTO>>> {
    return this.api.get(
      `/api/v1/hrms/employees?page=${page}&size=${size}`,
    ) as Observable<ApiResponse<Page<EmployeeProfileDTO>>>;
  }

  /** Enriched hub list — includes branch, region, correct status, profile image */
  getEmployeeHubList(
    page = 0,
    size = 100,
  ): Observable<ApiResponse<EmployeeHubPageDTO>> {
    return this.api.get(
      `/api/v1/hrms/employees/hub-list?page=${page}&size=${size}`,
    ) as Observable<ApiResponse<EmployeeHubPageDTO>>;
  }

  getEmployeeById(id: string): Observable<ApiResponse<EmployeeProfileDTO>> {
    return this.api.get(`/api/v1/hrms/employees/${id}`) as Observable<
      ApiResponse<EmployeeProfileDTO>
    >;
  }

  /** Full 9-section employee dashboard */
  getEmployeeDashboard(
    id: string,
  ): Observable<ApiResponse<EmployeeDashboardResponse>> {
    return this.api.get(`/api/v1/hrms/employees/${id}/dashboard`) as Observable<
      ApiResponse<EmployeeDashboardResponse>
    >;
  }
}
