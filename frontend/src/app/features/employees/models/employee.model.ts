// ── List / profile DTOs ──────────────────────────────────────────────────────
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

// ── Hub list DTOs ────────────────────────────────────────────────────────────
export interface EmployeeHubItemDTO {
  id: number;
  employeeCode: string;
  fullName: string;
  designation: string | null;
  department: string | null;
  branchName: string | null;
  regionName: string | null;
  status: string | null;
  profileImage: string | null;
  experienceYears: number;
}

export interface EmployeeHubPageDTO {
  content: EmployeeHubItemDTO[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

// ── Dashboard section DTOs ───────────────────────────────────────────────────
export interface ProfileHeaderDTO {
  employeeId: number;
  fullName: string;
  designation: string | null;
  department: string | null;
  profileImage: string | null;
  officialEmail: string;
}

export interface PersonalInfoDTO {
  officialEmail: string;
  phoneNumber: string | null;
  branch: string | null;
  region: string | null;
  joinDate: string | null;
  experienceYears: number;
  experienceMonths: number;
  totalExperience: string;
  reportingManagerName: string | null;
}

export interface AttendanceAnalyticsDTO {
  totalDays: number;
  presentDays: number;
  absentDays: number;
  attendancePercentage: number;
}

export interface PerformanceTrendDTO {
  month: string;
  score: number;
}

export interface SkillDTO {
  id: number;
  skillName: string;
  skillLevel: string | null;
}

export interface EmployeeProjectDTO {
  id: number;
  projectName: string;
  projectStatus: string | null;
  projectDescription: string | null;
  startDate: string | null;
  endDate: string | null;
  technologies: string[];
}

export interface EducationDTO {
  id: number;
  educationType: string | null;
  subject: string | null;
  institution: string | null;
  startYear: string | null;
  endDate: string | null;
  grade: string | null;
  description: string | null;
}

export interface AchievementDTO {
  id: number;
  title: string;
  description: string | null;
  achievementDate: string | null;
}

export interface CertificationDTO {
  id: number;
  certificationName: string;
  issuingOrganization: string | null;
  certificationDate: string | null;
}

export interface WorkExperienceDTO {
  id: number;
  companyName: string;
  jobTitle: string | null;
  startDate: string | null;
  endDate: string | null;
  description: string | null;
  status: string | null;
}

export interface EmployeeDashboardResponse {
  profileHeader: ProfileHeaderDTO;
  personalInfo: PersonalInfoDTO;
  attendanceAnalytics: AttendanceAnalyticsDTO;
  performanceTrends: PerformanceTrendDTO[];
  skills: SkillDTO[];
  projects: EmployeeProjectDTO[];
  education: EducationDTO[];
  achievements: AchievementDTO[];
  certifications: CertificationDTO[];
  workExperience: WorkExperienceDTO[];
}
