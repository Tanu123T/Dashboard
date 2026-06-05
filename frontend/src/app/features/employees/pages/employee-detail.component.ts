import { Component, OnInit, OnDestroy } from "@angular/core";
import { CommonModule } from "@angular/common";
import { ActivatedRoute, RouterModule } from "@angular/router";
import { EmployeeFeatureService } from "../services/employee-feature.service";
import { Subject, of } from "rxjs";
import { takeUntil, catchError, finalize } from "rxjs/operators";
import {
  EmployeeDashboardResponse,
  ProfileHeaderDTO,
  PersonalInfoDTO,
  AttendanceAnalyticsDTO,
  PerformanceTrendDTO,
  SkillDTO,
  EmployeeProjectDTO,
  EducationDTO,
  AchievementDTO,
  CertificationDTO,
  WorkExperienceDTO,
} from "../models/employee.model";

// ── View-model interfaces (what the HTML template binds to) ──────────────────

interface PersonalInfo {
  email: string;
  phone: string;
  department: string;
  location: string;
  joinDate: string;
  manager: string;
}

interface PerformanceTrend {
  month: string;
  value: number;
}

interface Project {
  name: string;
  status: "In Progress" | "Completed" | "On Hold";
  stages: string[];
  progress?: number;
}

interface WorkExperience {
  title: string;
  company: string;
  duration: string;
  description: string;
  years: string;
}

interface Education {
  degree: string;
  institution: string;
  field: string;
  year: string;
}

interface Achievement {
  title: string;
  date: string;
}

interface DetailItem {
  label: string;
  value: string;
}

interface EmployeeDetail {
  id: string;
  name: string;
  title: string;
  department: string;
  email: string;
  phone: string;
  photo?: string;
  initials: string;
  color: string;
  personalInfo: PersonalInfo;
  attendance: {
    totalDays: number;
    presentDays: number;
    absentDays: number;
    attendanceRate: number;
  };
  performanceTrends: PerformanceTrend[];
  skills: string[];
  projects: Project[];
  workExperience: WorkExperience[];
  education: Education[];
  achievements: Achievement[];
  detailItems: DetailItem[];
}

@Component({
  selector: "app-employee-detail",
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: "./employee-detail.component.html",
  styleUrls: ["./employee-detail.component.css"],
})
export class EmployeeDetailComponent implements OnInit, OnDestroy {
  employee: EmployeeDetail | null = null;
  loading = true;
  error: string | null = null;
  private destroy$ = new Subject<void>();

  private colorPalette = [
    "linear-gradient(135deg, #6f7ef7 0%, #5b6df0 45%, #7c8cff 100%)",
    "linear-gradient(135deg, #f7a34c 0%, #f89f35 45%, #fbb74f 100%)",
    "linear-gradient(135deg, #5aa7ff 0%, #4c8ef6 45%, #67d3d8 100%)",
    "linear-gradient(135deg, #7a6ee8 0%, #6d82f2 45%, #6bb1e5 100%)",
    "linear-gradient(135deg, #2ea79a 0%, #4ea7e1 45%, #73c7cf 100%)",
    "linear-gradient(135deg, #4a9df2 0%, #42b6da 45%, #4fd0c5 100%)",
  ];

  constructor(
    private route: ActivatedRoute,
    private employeeService: EmployeeFeatureService,
  ) {}

  ngOnInit() {
    this.route.params.pipe(takeUntil(this.destroy$)).subscribe((params) => {
      const employeeId = params["id"];
      if (employeeId) {
        this.loadEmployeeDashboard(employeeId);
      }
    });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ── Main loader ─────────────────────────────────────────────────────────────

  currentEmployeeId: string = "";

  loadEmployeeDashboard(employeeId: string) {
    this.loading = true;
    this.error = null;
    this.currentEmployeeId = employeeId;

    this.employeeService
      .getEmployeeDashboard(employeeId)
      .pipe(
        takeUntil(this.destroy$),
        catchError((err) => {
          const status = err?.status ?? 0;
          const detail =
            err?.error?.message || err?.error?.error || err?.message || "";
          if (status === 404) {
            this.error = `Employee #${employeeId} not found in HRMS. Please ensure the backend is running with the latest code.`;
          } else if (status === 0) {
            this.error =
              "Cannot reach the backend server. Please make sure it is running on http://localhost:8081";
          } else {
            this.error = `Server error (${status})${detail ? ": " + detail : ""}. Check the backend logs.`;
          }
          console.error(
            `Dashboard API error [${status}] for employee ${employeeId}:`,
            err,
          );
          return of(null);
        }),
        finalize(() => (this.loading = false)),
      )
      .subscribe((response: any) => {
        if (!response?.data) return;

        const d: EmployeeDashboardResponse = response.data;

        const header = d.profileHeader || ({} as ProfileHeaderDTO);
        const info = d.personalInfo || ({} as PersonalInfoDTO);
        const attend = d.attendanceAnalytics || ({} as AttendanceAnalyticsDTO);
        const perf = d.performanceTrends || [];
        const skills = d.skills || [];
        const projects = d.projects || [];
        const edu = d.education || [];
        const achieve = d.achievements || [];
        const certs = d.certifications || [];
        const workExp = d.workExperience || [];

        const fullName = header.fullName || "Unknown";
        const dept = header.department || "General";
        const desig = header.designation || "Staff";
        const empIdNum = Number(header.employeeId) || 0;

        this.employee = {
          id: String(header.employeeId || employeeId),
          name: fullName,
          title: `${desig} | ${dept}`,
          department: dept,
          email: header.officialEmail || "N/A",
          phone: info.phoneNumber || "N/A",
          photo: header.profileImage || undefined,
          initials: this.getInitials(fullName),
          color: this.colorPalette[empIdNum % this.colorPalette.length],

          personalInfo: {
            email: info.officialEmail || header.officialEmail || "N/A",
            phone: info.phoneNumber || "N/A",
            department: dept,
            location:
              [info.branch, info.region].filter(Boolean).join(", ") || dept,
            joinDate: this.formatDate(info.joinDate),
            manager: info.reportingManagerName || "N/A",
          },

          attendance: {
            totalDays: Number(attend.totalDays || 0),
            presentDays: Number(attend.presentDays || 0),
            absentDays: Number(attend.absentDays || 0),
            attendanceRate: Number(attend.attendancePercentage || 0),
          },

          // PerformanceTrendDTO has { month: string, score: number }
          performanceTrends: perf.map((p: PerformanceTrendDTO) => ({
            month: p.month,
            value: Number(p.score || 0),
          })),

          // SkillDTO has { id, skillName, skillLevel } — render as string badges
          skills: skills
            .map((s: SkillDTO) => s.skillName)
            .filter((name: string) => !!name),

          // EmployeeProjectDTO has { id, projectName, projectStatus, technologies[] }
          projects: projects.map((p: EmployeeProjectDTO) => ({
            name: p.projectName || "Unnamed Project",
            status: this.resolveProjectStatus(p.projectStatus),
            stages: Array.isArray(p.technologies) ? p.technologies : [],
            progress: 0,
          })),

          // WorkExperienceDTO has { companyName, jobTitle, startDate, endDate, description, status }
          workExperience: workExp.map((w: WorkExperienceDTO) => ({
            title: w.jobTitle || "Role",
            company: w.companyName || "",
            duration: this.formatRange(w.startDate, w.endDate),
            description: w.description || "",
            years: "",
          })),

          // EducationDTO has { educationType, subject, institution, startYear, endDate, grade }
          education: edu.map((e: EducationDTO) => ({
            degree: this.buildDegreeLabel(e),
            institution: e.institution || "",
            field: e.subject || e.grade || "",
            year: this.formatYearRange(e.startYear, e.endDate),
          })),

          // Merge achievements + certifications into a single flat list
          achievements: [
            ...achieve.map((a: AchievementDTO) => ({
              title: a.title || "",
              date: this.formatDate(a.achievementDate) || "",
            })),
            ...certs.map((c: CertificationDTO) => ({
              title: [c.certificationName, c.issuingOrganization]
                .filter(Boolean)
                .join(" — "),
              date: this.formatDate(c.certificationDate) || "",
            })),
          ],

          detailItems: this.buildDetailItems(header, info, attend),
        };
      });
  }

  // ── Chart helpers ────────────────────────────────────────────────────────────

  getInitials(fullName: string): string {
    const names = fullName.split(" ").filter((n) => n.length > 0);
    if (names.length === 0) return "??";
    if (names.length === 1) return names[0].substring(0, 2).toUpperCase();
    return (names[0][0] + names[names.length - 1][0]).toUpperCase();
  }

  getAttendanceLabel(rate: number): string {
    if (rate >= 90) return "Excellent";
    if (rate >= 80) return "Good";
    if (rate >= 70) return "Average";
    return "Needs Improvement";
  }

  getActiveProjectsCount(): number {
    if (!this.employee) return 0;
    return this.employee.projects.filter((p) => p.status === "In Progress")
      .length;
  }

  getPerformancePath(): string {
    const emp = this.employee;
    if (!emp || !emp.performanceTrends.length) {
      return "M 24 160 L 104 140 L 184 122 L 264 110 L 344 102 L 424 96 L 480 92";
    }
    const len = emp.performanceTrends.length;
    const points = emp.performanceTrends.map((pt, i) => ({
      x: 24 + (i * 456) / Math.max(1, len - 1),
      y: 170 - Math.min(140, Math.max(10, pt.value * 1.4)),
    }));
    return points
      .map(
        (pt, i) =>
          `${i === 0 ? "M" : "L"} ${pt.x.toFixed(0)} ${pt.y.toFixed(0)}`,
      )
      .join(" ");
  }

  getPerformanceAreaPath(): string {
    const line = this.getPerformancePath();
    const len = this.employee?.performanceTrends.length || 1;
    const endX = 24 + (Math.max(0, len - 1) * 456) / Math.max(1, len - 1);
    return `${line} L ${endX.toFixed(0)} 190 L 24 190 Z`;
  }

  // ── Private helpers ──────────────────────────────────────────────────────────

  private buildDegreeLabel(e: EducationDTO): string {
    const type = e.educationType || "";
    const subj = e.subject || "";
    if (type && subj) return `${type} — ${subj}`;
    return type || subj || e.description || "Education";
  }

  private resolveProjectStatus(
    status: string | null | undefined,
  ): "In Progress" | "Completed" | "On Hold" {
    const s = (status || "").toLowerCase();
    if (s.includes("hold")) return "On Hold";
    if (s.includes("progress") || s.includes("active")) return "In Progress";
    if (s.includes("complete") || s.includes("done")) return "Completed";
    return "In Progress";
  }

  private formatDate(value: string | null | undefined): string {
    if (!value) return "N/A";
    return String(value).includes("T")
      ? String(value).split("T")[0]
      : String(value);
  }

  private formatRange(
    start: string | null | undefined,
    end: string | null | undefined,
  ): string {
    const s = this.formatDate(start);
    const e = end ? this.formatDate(end) : "Present";
    return `${s} — ${e}`;
  }

  private formatYearRange(
    startYear: string | null | undefined,
    endDate: string | null | undefined,
  ): string {
    const s = startYear || "";
    const e = endDate ? String(endDate).substring(0, 4) : "Present";
    return s ? `${s} — ${e}` : e;
  }

  private buildDetailItems(
    header: ProfileHeaderDTO,
    info: PersonalInfoDTO,
    attend: AttendanceAnalyticsDTO,
  ): DetailItem[] {
    const rows: Array<{
      label: string;
      value: string | null | undefined | number;
    }> = [
      { label: "Employee ID", value: header.employeeId },
      { label: "Official Email", value: header.officialEmail },
      { label: "Designation", value: header.designation },
      { label: "Department", value: header.department },
      { label: "Phone", value: info.phoneNumber },
      { label: "Branch", value: info.branch },
      { label: "Region", value: info.region },
      { label: "Join Date", value: this.formatDate(info.joinDate) },
      { label: "Total Experience", value: info.totalExperience },
      { label: "Reporting Manager", value: info.reportingManagerName },
      { label: "Attendance %", value: `${attend.attendancePercentage ?? 0}%` },
      { label: "Present Days", value: attend.presentDays },
      { label: "Absent Days", value: attend.absentDays },
      { label: "Total Days", value: attend.totalDays },
    ];

    return rows
      .filter(
        (r) =>
          r.value !== null &&
          r.value !== undefined &&
          r.value !== "" &&
          r.value !== "N/A",
      )
      .map((r) => ({ label: r.label, value: String(r.value) }));
  }
}
