import { Component, OnInit, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { UserService, UserProfile } from '../../core/services/user.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import keycloak from '../../keycloak.service';
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
  status: "connected" | "disconnected";
}

@Component({
  selector: "app-settings",
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css']
})
export class SettingsComponent implements OnInit, OnDestroy {
  profile = {
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    designation: '',
    department: ''
  };

  preferences = {
    emailAlerts: true
  };

  isEditing = false;
  isSaving = false;
  saveMessage = '';
  saveError = '';
  isLoading = true;

  private destroy$ = new Subject<void>();

  constructor(
    private userService: UserService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    console.log('Settings component initialized');
    
    // Subscribe to user profile from UserService
    this.userService.getProfile()
      .pipe(takeUntil(this.destroy$))
      .subscribe((userProfile) => {
        console.log('Settings: Received user profile:', userProfile);
        
        if (userProfile) {
          this.profile = {
            firstName: userProfile.firstName || '',
            lastName: userProfile.lastName || '',
            email: userProfile.email || '',
            phone: userProfile.phone || '',
            designation: userProfile.designation || 'Chief Executive Officer',
            department: userProfile.department || 'Executive'
          };
          
          this.isLoading = false;
          console.log('Settings: Profile updated:', this.profile);
        } else {
          console.warn('Settings: No user profile available');
          this.isLoading = false;
        }
        
        // Force change detection
        this.cdr.detectChanges();
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  saveProfile(): void {
    if (!this.isEditing) {
      // Enter edit mode
      this.isEditing = true;
      this.cdr.detectChanges();
      return;
    }

    // Validate required fields
    if (!this.profile.firstName || !this.profile.firstName.trim()) {
      this.saveError = 'First name is required';
      this.cdr.detectChanges();
      return;
    }

  toggleSecurity(key: string): void {
    if (key === "twoFactorAuth") {
      this.security.twoFactorAuth = !this.security.twoFactorAuth;
    } else if (key === "loginAlerts") {
      this.security.loginAlerts = !this.security.loginAlerts;
    if (!this.profile.lastName || !this.profile.lastName.trim()) {
      this.saveError = 'Last name is required';
      this.cdr.detectChanges();
      return;
    }

  connectApp(appName: string): void {
    console.log("Connect to:", appName);
  }

  disconnectApp(appName: string): void {
    console.log("Disconnect from:", appName);
  }

  deactivateAccount(): void {
    if (
      confirm(
        "Are you sure you want to deactivate your account? This action cannot be undone.",
      )
    ) {
      console.log("Account deactivation initiated");
    if (!this.profile.email || !this.profile.email.trim()) {
      this.saveError = 'Email is required';
      this.cdr.detectChanges();
      return;
    }

    this.isSaving = true;
    this.saveError = '';
    this.saveMessage = '';
    this.cdr.detectChanges();

    console.log('Settings: Attempting to save profile...');

    // Call UserService to update profile
    const userProfile: UserProfile = {
      firstName: this.profile.firstName.trim(),
      lastName: this.profile.lastName.trim(),
      email: this.profile.email.trim(),
      phone: this.profile.phone?.trim() || '',
      designation: this.profile.designation,
      department: this.profile.department
    };

    this.userService.updateProfile(userProfile)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          console.log('Settings: Profile saved successfully:', response);
          this.isSaving = false;
          this.isEditing = false;
          this.saveMessage = 'Profile updated successfully!';
          
          // Clear message after 3 seconds
          setTimeout(() => {
            this.saveMessage = '';
            this.cdr.detectChanges();
          }, 3000);
          
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Settings: Error saving profile:', error);
          this.isSaving = false;
          
          // Check if error is due to backend not available
          if (error.status === 0 || error.status === 404) {
            // Backend not available, save to localStorage as fallback
            console.warn('Settings: Backend not available, saving to localStorage');
            try {
              localStorage.setItem('userProfile', JSON.stringify(userProfile));
              this.isEditing = false;
              this.saveMessage = 'Profile saved (local)';
              
              setTimeout(() => {
                this.saveMessage = '';
                this.cdr.detectChanges();
              }, 3000);
            } catch (storageError) {
              console.error('Settings: Failed to save to localStorage:', storageError);
              this.saveError = 'Failed to save profile. Please try again.';
            }
          } else {
            this.saveError = error.error?.message || 'Failed to save profile. Please try again.';
          }
          
          this.cdr.detectChanges();
        }
      });
  }

  cancelEdit(): void {
    this.isEditing = false;
    this.saveError = '';
    this.saveMessage = '';
    
    // Reload profile from service
    const currentProfile = this.userService.getCurrentProfile();
    if (currentProfile) {
      this.profile = {
        firstName: currentProfile.firstName,
        lastName: currentProfile.lastName,
        email: currentProfile.email,
        phone: currentProfile.phone,
        designation: currentProfile.designation,
        department: currentProfile.department
      };
    }
    
    this.cdr.detectChanges();
  }

  removeDevice(deviceName: string): void {
    if (confirm(`Remove ${deviceName}?`)) {
      this.devices = this.devices.filter((d) => d.name !== deviceName);
  changePassword(): void {
    const kc = keycloak as any;
    
    if (!kc || !kc.authenticated) {
      this.saveError = 'Not authenticated. Please log in again.';
      this.cdr.detectChanges();
      return;
    }

    try {
      console.log('Settings: Opening Keycloak account management for password change');
      
      // Try to open Keycloak account management page
      if (kc && typeof kc.accountManagement === 'function') {
        kc.accountManagement();
      } else {
        // Fallback: construct account URL manually
        const accountUrl = `${kc.authServerUrl}/realms/${kc.realm}/account`;
        console.log('Settings: Opening account URL:', accountUrl);
        window.open(accountUrl, '_blank');
      }
      
      this.saveMessage = 'Opening password management in a new window...';
      setTimeout(() => {
        this.saveMessage = '';
        this.cdr.detectChanges();
      }, 3000);
      
      this.cdr.detectChanges();
    } catch (error) {
      console.error('Settings: Error opening password change:', error);
      this.saveError = 'Failed to open password management. Please try again.';
      this.cdr.detectChanges();
    }
  }

  toggleEmailAlerts(): void {
    this.preferences.emailAlerts = !this.preferences.emailAlerts;
    console.log('Settings: Email alerts toggled:', this.preferences.emailAlerts);
    this.cdr.detectChanges();
  }
}
