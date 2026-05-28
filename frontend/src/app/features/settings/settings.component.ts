import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';

interface Device {
  name: string;
  type: string;
  location: string;
  lastActive: string;
  status: string;
}

interface ConnectedApp {
  name: string;
  description: string;
  icon: string;
  status: 'connected' | 'disconnected';
}

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css']
})
export class SettingsComponent implements OnInit {
  profile = {
    firstName: 'John',
    lastName: 'Doe',
    email: 'ceo@company.com',
    phone: '+91 98765 43210',
    timezone: 'Asia/Kolkata',
    role: 'Chief Executive Officer'
  };

  preferences = {
    emailAlerts: true,
    pushNotifications: true,
    weeklyDigest: false
  };

  security = {
    twoFactorAuth: true,
    loginAlerts: true
  };

  devices: Device[] = [
    {
      name: 'Windows Laptop',
      type: 'Windows',
      location: 'Pune, IN',
      lastActive: 'Today, 10:14 AM',
      status: 'active'
    },
    {
      name: 'iPhone 15',
      type: 'iOS',
      location: 'Mumbai, IN',
      lastActive: 'Today, 10:14 AM',
      status: 'inactive'
    },
    {
      name: 'MacBook Air',
      type: 'macOS',
      location: 'Bengaluru, IN',
      lastActive: 'Yesterday, 06:49 PM',
      status: 'inactive'
    }
  ];

  connectedApps: ConnectedApp[] = [
    {
      name: 'Slack Workspace',
      description: 'Notifications + profile',
      icon: 'slack',
      status: 'disconnected'
    },
    {
      name: 'Google Calendar',
      description: 'Meeting sync',
      icon: 'calendar',
      status: 'disconnected'
    },
    {
      name: 'Jira Cloud',
      description: 'Task updates',
      icon: 'briefcase',
      status: 'connected'
    }
  ];

  constructor() { }

  ngOnInit(): void {
  }

  saveProfile(): void {
    console.log('Profile saved:', this.profile);
  }

  changePassword(): void {
    console.log('Change password');
  }

  togglePreference(key: string): void {
    if (key === 'emailAlerts') {
      this.preferences.emailAlerts = !this.preferences.emailAlerts;
    } else if (key === 'pushNotifications') {
      this.preferences.pushNotifications = !this.preferences.pushNotifications;
    } else if (key === 'weeklyDigest') {
      this.preferences.weeklyDigest = !this.preferences.weeklyDigest;
    }
  }

  toggleSecurity(key: string): void {
    if (key === 'twoFactorAuth') {
      this.security.twoFactorAuth = !this.security.twoFactorAuth;
    } else if (key === 'loginAlerts') {
      this.security.loginAlerts = !this.security.loginAlerts;
    }
  }

  connectApp(appName: string): void {
    console.log('Connect to:', appName);
  }

  disconnectApp(appName: string): void {
    console.log('Disconnect from:', appName);
  }

  deactivateAccount(): void {
    if (confirm('Are you sure you want to deactivate your account? This action cannot be undone.')) {
      console.log('Account deactivation initiated');
    }
  }

  removeDevice(deviceName: string): void {
    if (confirm(`Remove ${deviceName}?`)) {
      this.devices = this.devices.filter(d => d.name !== deviceName);
    }
  }
}
