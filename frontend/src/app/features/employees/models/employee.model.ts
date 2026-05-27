export interface EmployeeProfileDTO {
  id: number;
  employeeCode: string;
  fullName: string;
  officialEmail: string;
  profileImage: string | null;
  designation: string | null;
  department: string | null;
  workMode: string | null;
  employmentType: string | null;
  employeeStatus: string | null;
  reportingManagerName: string | null;
}

export interface Page<T> {
  content: T[];
  empty: boolean;
  first: boolean;
  last: boolean;
  number: number;
  numberOfElements: number;
  size: number;
  totalElements: number;
  totalPages: number;
  pageable: any;
}

export interface ApiResponse<T> {
  status: number;
  message: string;
  data: T;
  error?: string;
  timestamp: string;
}
