import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

interface EmployeeCard {
  id: string;
  initials: string;
  name: string;
  title: string;
  status: 'Active' | 'Inactive' | 'On Leave';
  department: string;
  location: string;
  stability: number;
  color: string;
}

@Component({
  selector: 'app-employee-hub',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './employee-hub.component.html',
  styleUrls: ['./employee-hub.component.css']
})
export class EmployeeHubComponent implements OnInit {
  searchQuery = '';
  showAllEmployees = false;
  employees: EmployeeCard[] = [
    {
      id: 'E001',
      initials: 'SC',
      name: 'Sarah Chen',
      title: 'Engineering Manager',
      status: 'Active',
      department: 'Engineering',
      location: 'Bengaluru',
      stability: 91,
      color: 'linear-gradient(135deg, #6f7ef7 0%, #5b6df0 45%, #7c8cff 100%)'
    },
    {
      id: 'E002',
      initials: 'JW',
      name: 'James Wilson',
      title: 'Sales Director',
      status: 'Active',
      department: 'Sales',
      location: 'Mumbai',
      stability: 86,
      color: 'linear-gradient(135deg, #f7a34c 0%, #f89f35 45%, #fbb74f 100%)'
    },
    {
      id: 'E003',
      initials: 'PP',
      name: 'Priya Patel',
      title: 'Product Lead',
      status: 'Active',
      department: 'Product',
      location: 'Pune',
      stability: 89,
      color: 'linear-gradient(135deg, #5aa7ff 0%, #4c8ef6 45%, #67d3d8 100%)'
    },
    {
      id: 'E004',
      initials: 'ML',
      name: 'Marcus Lee',
      title: 'Marketing Manager',
      status: 'Active',
      department: 'Marketing',
      location: 'Delhi',
      stability: 78,
      color: 'linear-gradient(135deg, #7a6ee8 0%, #6d82f2 45%, #6bb1e5 100%)'
    },
    {
      id: 'E005',
      initials: 'ET',
      name: 'Elena Torres',
      title: 'Design Lead',
      status: 'Active',
      department: 'Design',
      location: 'Remote',
      stability: 84,
      color: 'linear-gradient(135deg, #2ea79a 0%, #4ea7e1 45%, #73c7cf 100%)'
    },
    {
      id: 'E006',
      initials: 'AK',
      name: 'Alex Kim',
      title: 'Frontend Engineer',
      status: 'Active',
      department: 'Engineering',
      location: 'Bengaluru',
      stability: 81,
      color: 'linear-gradient(135deg, #4a9df2 0%, #42b6da 45%, #4fd0c5 100%)'
    },
    {
      id: 'E007',
      initials: 'AK',
      name: 'Anna Kowalski',
      title: 'HR Director',
      status: 'Active',
      department: 'HR',
      location: 'Bengaluru',
      stability: 93,
      color: 'linear-gradient(135deg, #6f7ef7 0%, #5f7ef0 45%, #4fa2f3 100%)'
    },
    {
      id: 'E008',
      initials: 'LW',
      name: 'Li Wei',
      title: 'Data Scientist',
      status: 'Active',
      department: 'Engineering',
      location: 'Hyderabad',
      stability: 82,
      color: 'linear-gradient(135deg, #f7a34c 0%, #f7b547 45%, #f5c45a 100%)'
    },
    {
      id: 'E009',
      initials: 'RO',
      name: 'Ryan O Brien',
      title: 'Sales Manager',
      status: 'Active',
      department: 'Sales',
      location: 'Mumbai',
      stability: 76,
      color: 'linear-gradient(135deg, #5aa7ff 0%, #57b6e3 45%, #68d0ca 100%)'
    },
    {
      id: 'E010',
      initials: 'SG',
      name: 'Sofia Garcia',
      title: 'Marketing Lead',
      status: 'On Leave',
      department: 'Marketing',
      location: 'Delhi',
      stability: 71,
      color: 'linear-gradient(135deg, #7a6ee8 0%, #8a79ea 45%, #6bb1e5 100%)'
    },
    {
      id: 'E011',
      initials: 'MS',
      name: 'Maya Singh',
      title: 'Product Designer',
      status: 'Active',
      department: 'Design',
      location: 'Remote',
      stability: 74,
      color: 'linear-gradient(135deg, #2ea79a 0%, #4ea7e1 45%, #73c7cf 100%)'
    },
    {
      id: 'E012',
      initials: 'DB',
      name: 'David Brown',
      title: 'Finance Controller',
      status: 'Active',
      department: 'Finance',
      location: 'Bengaluru',
      stability: 88,
      color: 'linear-gradient(135deg, #4a9df2 0%, #42b6da 45%, #4fd0c5 100%)'
    }
  ];

  filteredEmployees: EmployeeCard[] = [];

  ngOnInit() {
    this.filterEmployees();
  }

  filterEmployees() {
    const query = this.searchQuery.toLowerCase();
    const matchingEmployees = !query ? this.employees : this.employees.filter(emp =>
        emp.name.toLowerCase().includes(query) ||
        emp.title.toLowerCase().includes(query) ||
        emp.department.toLowerCase().includes(query) ||
        emp.location.toLowerCase().includes(query)
      );

    this.filteredEmployees = this.showAllEmployees || query ? matchingEmployees : matchingEmployees.slice(0, 6);
  }

  onSearchChange() {
    this.filterEmployees();
  }

  toggleShowAllEmployees() {
    this.showAllEmployees = !this.showAllEmployees;
    this.filterEmployees();
  }

  getShowCount(): string {
    return `Complete member directory - ${this.filteredEmployees.length} of ${this.employees.length} shown`;
  }

  getViewAllLabel(): string {
    return this.showAllEmployees ? 'Show Less' : 'View All';
  }
}
