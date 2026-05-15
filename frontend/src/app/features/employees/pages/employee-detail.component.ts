import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';

interface PersonalInfo {
  email: string;
  phone: string;
  department: string;
  joinDate: string;
}

interface PerformanceTrend {
  month: string;
  value: number;
}

interface Project {
  name: string;
  status: 'In Progress' | 'Completed' | 'On Hold';
  stages: string[];  // Tech stack items
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
  attendanceRate: number;
  performanceTrends: PerformanceTrend[];
  skills: string[];
  projects: Project[];
  workExperience: WorkExperience[];
  education: Education[];
  achievements: Achievement[];
}

@Component({
  selector: 'app-employee-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './employee-detail.component.html',
  styleUrls: ['./employee-detail.component.css']
})
export class EmployeeDetailComponent implements OnInit {
  employee: EmployeeDetail | null = null;
  loading = true;
  error: string | null = null;

  constructor(private route: ActivatedRoute) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      const employeeId = params['id'];
      if (employeeId) {
        this.loadEmployeeDetail(employeeId);
      }
    });
  }

  loadEmployeeDetail(employeeId: string) {
    this.loading = true;
    // Mock data - replace with actual service call
    this.employee = this.getMockEmployeeDetail(employeeId);
    this.loading = false;
  }

  getMockEmployeeDetail(employeeId: string): EmployeeDetail {
    return {
      id: employeeId,
      name: 'Sarah Chen',
      title: 'Engineering Manager | Engineering',
      department: 'Engineering',
      email: 'sarah.chen@company.com',
      phone: '+91 9876543210',
      initials: 'SC',
      color: 'linear-gradient(135deg, #6f7ef7 0%, #5b6df0 45%, #7c8cff 100%)',
      personalInfo: {
        email: 'sarah.chen@company.com',
        phone: '+91 9876543210',
        department: 'Engineering',
        joinDate: 'Jan 15, 2020'
      },
      attendanceRate: 87,
      performanceTrends: [
        { month: 'Jan', value: 70 },
        { month: 'Feb', value: 75 },
        { month: 'Mar', value: 80 },
        { month: 'Apr', value: 78 },
        { month: 'May', value: 85 },
        { month: 'Jun', value: 87 },
        { month: 'Jul', value: 89 },
        { month: 'Aug', value: 92 }
      ],
      skills: ['JavaScript', 'React', 'Node.js', 'TypeScript'],
      projects: [
        {
          name: 'Enterprise CRM Overhaul',
          status: 'In Progress',
          stages: ['React', 'Node.js', 'PostgreSQL', 'Redis']
        },
        {
          name: 'AI Analytics Dashboard',
          status: 'Completed',
          stages: ['React', 'Python', 'TensorFlow', 'Node.js']
        },
        {
          name: 'Multi-Tenant Auth System',
          status: 'Completed',
          stages: ['Node.js', 'PostgreSQL', 'OAuth2']
        }
      ],
      workExperience: [
        {
          title: 'Senior Software Engineer',
          company: 'Tech Corporation',
          duration: '2021 - PRESENT',
          description: 'Leading development of enterprise applications and mentor junior developers',
          years: '5 Years'
        },
        {
          title: 'Full Stack Developer & DevOps',
          company: 'Digital Solutions Inc',
          duration: '2020 - 2021',
          description: 'Developed and maintained mobile applications with micro-services architecture',
          years: '2 Years'
        },
        {
          title: 'Junior Developer',
          company: 'StartUp Labs',
          duration: '2019 - 2020',
          description: 'Built REST backend components and API integrations',
          years: '1 Year'
        }
      ],
      education: [
        {
          degree: 'B.S. in Computer Science',
          institution: 'Stanford University',
          field: 'Computer Science',
          year: '2019'
        },
        {
          degree: 'M.S. in Software Engineering',
          institution: 'MIT',
          field: 'Software Engineering',
          year: '2021'
        }
      ],
      achievements: [
        {
          title: 'Best Employee of the Year 2023',
          date: '2023'
        },
        {
          title: 'EarlyBird Innovator- Q4/25',
          date: '2024'
        },
        {
          title: 'Team Player of the Month 2023',
          date: '2023'
        }
      ]
    };
  }

  getAttendanceLabel(rate: number): string {
    if (rate >= 90) return 'Excellent';
    if (rate >= 80) return 'Good';
    if (rate >= 70) return 'Average';
    return 'Needs Improvement';
  }

  getActiveProjectsCount(): number {
    if (!this.employee) return 0;
    return this.employee.projects.filter(p => p.status === 'In Progress').length;
  }
}
