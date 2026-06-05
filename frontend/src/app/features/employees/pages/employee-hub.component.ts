import { Component, OnInit, OnDestroy } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { Router, RouterModule } from "@angular/router";
import { EmployeeFeatureService } from "../services/employee-feature.service";
import {
  EmployeeHubItemDTO,
  EmployeeProfileDTO,
} from "../models/employee.model";
import { Subject, timer, Subscription, of } from "rxjs";
import { switchMap, catchError, takeUntil, map } from "rxjs/operators";

interface EmployeeCard {
  id: string;
  initials: string;
  name: string;
  title: string;
  statusLabel: string;
  statusClass: string;
  department: string;
  location: string;
  stability: number;
  color: string;
  profileImage: string | null;
}

@Component({
  selector: "app-employee-hub",
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: "./employee-hub.component.html",
  styleUrls: ["./employee-hub.component.css"],
})
export class EmployeeHubComponent implements OnInit, OnDestroy {
  searchQuery = "";
  showAllEmployees = false;
  employees: EmployeeCard[] = [];
  filteredEmployees: EmployeeCard[] = [];
  isLoading = true;
  errorMessage = "";

  private destroy$ = new Subject<void>();
  private pollingSubscription?: Subscription;

  private colorPalette = [
    "linear-gradient(135deg, #6f7ef7 0%, #5b6df0 45%, #7c8cff 100%)",
    "linear-gradient(135deg, #f7a34c 0%, #f89f35 45%, #fbb74f 100%)",
    "linear-gradient(135deg, #5aa7ff 0%, #4c8ef6 45%, #67d3d8 100%)",
    "linear-gradient(135deg, #7a6ee8 0%, #6d82f2 45%, #6bb1e5 100%)",
    "linear-gradient(135deg, #2ea79a 0%, #4ea7e1 45%, #73c7cf 100%)",
    "linear-gradient(135deg, #4a9df2 0%, #42b6da 45%, #4fd0c5 100%)",
  ];

  constructor(
    private router: Router,
    private employeeService: EmployeeFeatureService,
  ) {}

  ngOnInit() {
    this.startPolling();
  }
  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ── Data loading with automatic fallback ──────────────────────────────────
  private startPolling() {
    this.pollingSubscription = timer(0, 60000)
      .pipe(
        takeUntil(this.destroy$),
        switchMap(() => this.loadEmployees()),
      )
      .subscribe((cards) => {
        this.isLoading = false;
        if (cards !== null) {
          this.employees = cards;
          this.filterEmployees();
          this.errorMessage = "";
        }
      });
  }

  /**
   * Try the new enriched /hub-list endpoint first.
   * If it returns any error (404 = old backend, 500 = SQL issue),
   * silently fall back to the original /employees endpoint.
   */
  private loadEmployees() {
    return this.employeeService.getEmployeeHubList(0, 1000).pipe(
      map((response) => {
        const items = response?.data?.content ?? [];
        if (items.length === 0 && (response?.data?.totalElements ?? 0) === 0) {
          // Hub-list returned but empty — possibly SQL issue; treat as fallback signal
          return null;
        }
        return this.mapHubItems(items);
      }),
      catchError(() =>
        // New endpoint failed (404, 500, network) — fall back silently
        this.employeeService.getEmployees(0, 1000).pipe(
          map((resp) => this.mapProfileDTOs(resp?.data?.content ?? [])),
          catchError((err) => {
            console.error("Both employee endpoints failed", err);
            this.errorMessage =
              "Unable to load employee data. Please ensure the backend is running on http://localhost:8081";
            return of(null);
          }),
        ),
      ),
      // If hub-list returned null (empty), also fall back
      switchMap((result) => {
        if (result === null) {
          return this.employeeService.getEmployees(0, 1000).pipe(
            map((resp) => this.mapProfileDTOs(resp?.data?.content ?? [])),
            catchError((err) => {
              console.error("Fallback employees endpoint also failed", err);
              this.errorMessage =
                "Unable to load employee data. Please ensure the backend is running on http://localhost:8081";
              return of(null);
            }),
          );
        }
        return of(result);
      }),
    );
  }

  // ── Mappers ────────────────────────────────────────────────────────────────

  private mapHubItems(dtos: EmployeeHubItemDTO[]): EmployeeCard[] {
    return dtos.map((dto, index) => {
      const names = (dto.fullName || "").split(" ").filter((n) => n.length > 0);
      const initials =
        names.length >= 2
          ? (names[0][0] + names[names.length - 1][0]).toUpperCase()
          : (names[0]?.substring(0, 2) ?? "EM").toUpperCase();

      const { label: statusLabel, cls: statusClass } = this.resolveStatus(
        dto.status,
      );
      const location = dto.branchName || dto.regionName || dto.department || "";
      const stability = Math.min(
        100,
        Math.max(10, (dto.experienceYears || 0) * 20),
      );

      return {
        id: String(dto.id),
        initials,
        name: dto.fullName || "Unknown",
        title: dto.designation || "Staff",
        statusLabel,
        statusClass,
        department: dto.department || "General",
        location,
        stability,
        color: this.colorPalette[(dto.id || index) % this.colorPalette.length],
        profileImage: dto.profileImage || null,
      };
    });
  }

  private mapProfileDTOs(dtos: EmployeeProfileDTO[]): EmployeeCard[] {
    return dtos.map((dto, index) => {
      const names = (dto.fullName || "").split(" ").filter((n) => n.length > 0);
      const initials =
        names.length >= 2
          ? (names[0][0] + names[names.length - 1][0]).toUpperCase()
          : (names[0]?.substring(0, 2) ?? "EM").toUpperCase();

      const { label: statusLabel, cls: statusClass } = this.resolveStatus(
        dto.employeeStatus,
      );

      return {
        id: String(dto.id),
        initials,
        name: dto.fullName || "Unknown",
        title: dto.designation || "Staff",
        statusLabel,
        statusClass,
        department: dto.department || "General",
        location: dto.department || "",
        stability: 85 + (index % 10),
        color: this.colorPalette[(dto.id || index) % this.colorPalette.length],
        profileImage: dto.profileImage || null,
      };
    });
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  private resolveStatus(raw: string | null | undefined): {
    label: string;
    cls: string;
  } {
    const s = (raw || "").toLowerCase().trim();
    if (!s) return { label: "Active", cls: "status-active" };
    if (
      s === "active" ||
      s === "1" ||
      s === "true" ||
      s === "working" ||
      s === "employed"
    )
      return { label: "Active", cls: "status-active" };
    if (s.includes("leave"))
      return { label: "On Leave", cls: "status-on-leave" };
    if (
      s === "inactive" ||
      s === "0" ||
      s === "false" ||
      s === "resigned" ||
      s === "terminated"
    )
      return { label: "Inactive", cls: "status-inactive" };
    // Unknown → treat as Active (they appear in the list)
    return { label: "Active", cls: "status-active" };
  }

  // ── Filtering & navigation ─────────────────────────────────────────────────

  filterEmployees() {
    const q = this.searchQuery.toLowerCase();
    const matched = !q
      ? this.employees
      : this.employees.filter(
          (e) =>
            e.name.toLowerCase().includes(q) ||
            e.title.toLowerCase().includes(q) ||
            e.department.toLowerCase().includes(q) ||
            e.location.toLowerCase().includes(q),
        );
    this.filteredEmployees =
      this.showAllEmployees || q ? matched : matched.slice(0, 6);
  }

  onSearchChange() {
    this.filterEmployees();
  }
  toggleShowAllEmployees() {
    this.showAllEmployees = !this.showAllEmployees;
    this.filterEmployees();
  }
  viewEmployeeDetail(id: string) {
    this.router.navigate(["/dashboard/employee-detail", id]);
  }

  getShowCount() {
    return `Complete member directory - ${this.filteredEmployees.length} of ${this.employees.length} shown`;
  }
  getViewAllLabel() {
    return this.showAllEmployees ? "Show Less" : "View All";
  }
}
