import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, BehaviorSubject } from 'rxjs';
import { map, catchError, tap } from 'rxjs/operators';
import { environment } from 'src/environments/environment';
import keycloak from '../../keycloak.service';

export interface UserProfile {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  designation: string;
  department: string;
  employeeCode?: string;
  profileImage?: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private userProfileSubject = new BehaviorSubject<UserProfile | null>(null);
  public userProfile$ = this.userProfileSubject.asObservable();

  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {
    this.loadUserProfile();
  }

  /**
   * Load user profile from Keycloak token and/or backend API
   */
  loadUserProfile(): void {
    const kc = keycloak as any;

    console.log('UserService: Loading user profile...');
    console.log('Keycloak authenticated:', kc?.authenticated);
    console.log('Keycloak token parsed:', kc?.tokenParsed);

    if (!kc || !kc.authenticated || !kc.tokenParsed) {
      console.warn('UserService: Keycloak not authenticated');
      
      // Try to load from localStorage as fallback
      try {
        const savedProfile = localStorage.getItem('userProfile');
        if (savedProfile) {
          const profile = JSON.parse(savedProfile) as UserProfile;
          console.log('UserService: Loaded profile from localStorage:', profile);
          this.userProfileSubject.next(profile);
        }
      } catch (error) {
        console.warn('UserService: Failed to load from localStorage:', error);
      }
      
      return;
    }

    try {
      const tokenParsed = kc.tokenParsed;
      const profile: UserProfile = {
        firstName: tokenParsed.given_name || '',
        lastName: tokenParsed.family_name || '',
        email: tokenParsed.email || '',
        phone: tokenParsed.phone_number || '',
        designation: tokenParsed.designation || '',
        department: tokenParsed.department || '',
        employeeCode: tokenParsed.preferred_username || '',
        profileImage: tokenParsed.picture || ''
      };

      console.log('UserService: Extracted from Keycloak token:', profile);

      // If we have basic info from token, update the subject
      if (profile.firstName && profile.lastName && profile.email) {
        this.userProfileSubject.next(profile);
        console.log('UserService: Profile loaded from Keycloak token');

        // Also save to localStorage as backup
        try {
          localStorage.setItem('userProfile', JSON.stringify(profile));
          console.log('UserService: Profile saved to localStorage');
        } catch (error) {
          console.warn('UserService: Failed to save to localStorage:', error);
        }

        // Try to fetch full profile from backend if available
        if (tokenParsed.sub) {
          this.fetchFullProfileFromBackend(tokenParsed.sub, profile).subscribe();
        }
      } else {
        console.warn('UserService: Missing required fields in Keycloak token');
        
        // Try to load from localStorage as fallback
        try {
          const savedProfile = localStorage.getItem('userProfile');
          if (savedProfile) {
            const profile = JSON.parse(savedProfile) as UserProfile;
            console.log('UserService: Loaded profile from localStorage as fallback:', profile);
            this.userProfileSubject.next(profile);
          }
        } catch (error) {
          console.warn('UserService: Failed to load from localStorage:', error);
        }
      }
    } catch (error) {
      console.error('UserService: Error loading profile:', error);
      
      // Try to load from localStorage as fallback
      try {
        const savedProfile = localStorage.getItem('userProfile');
        if (savedProfile) {
          const profile = JSON.parse(savedProfile) as UserProfile;
          console.log('UserService: Loaded profile from localStorage after error:', profile);
          this.userProfileSubject.next(profile);
        }
      } catch (storageError) {
        console.warn('UserService: Failed to load from localStorage:', storageError);
      }
    }
  }

  /**
   * Fetch full user profile from backend API
   */
  private fetchFullProfileFromBackend(userId: string, basicProfile: UserProfile): Observable<UserProfile> {
    const url = `${this.baseUrl}/api/v1/hrms/employees/${userId}/full`;

    console.log('UserService: Fetching full profile from backend:', url);

    return this.http.get<any>(url).pipe(
      tap((response) => {
        console.log('UserService: Backend response:', response);

        // Merge backend data with Keycloak data
        const enrichedProfile: UserProfile = {
          ...basicProfile,
          designation: response.designation || basicProfile.designation,
          department: response.department || basicProfile.department,
          phone: response.phone_number || response.phone || basicProfile.phone,
          profileImage: response.profileImage || basicProfile.profileImage,
          employeeCode: response.employeeCode || basicProfile.employeeCode
        };

        this.userProfileSubject.next(enrichedProfile);
        console.log('UserService: Profile enriched with backend data:', enrichedProfile);
      }),
      map(() => this.userProfileSubject.value!),
      catchError((error) => {
        console.warn('UserService: Failed to fetch full profile from backend:', error);
        // Return the basic profile from Keycloak if backend fails
        return of(basicProfile);
      })
    );
  }

  /**
   * Get current user profile synchronously
   */
  getCurrentProfile(): UserProfile | null {
    return this.userProfileSubject.value;
  }

  /**
   * Get user profile as Observable
   */
  getProfile(): Observable<UserProfile | null> {
    return this.userProfile$;
  }

  /**
   * Update user profile
   */
  updateProfile(profile: UserProfile): Observable<any> {
    const url = `${this.baseUrl}/api/v1/hrms/employees/profile`;
    
    console.log('UserService: Attempting to update profile at:', url);
    console.log('UserService: Profile data:', profile);

    return this.http.put(url, profile).pipe(
      tap((response) => {
        console.log('UserService: Profile updated successfully on backend');
        // Update the BehaviorSubject with the new profile
        this.userProfileSubject.next(profile);
        
        // Also save to localStorage as backup
        try {
          localStorage.setItem('userProfile', JSON.stringify(profile));
          console.log('UserService: Profile saved to localStorage');
        } catch (error) {
          console.warn('UserService: Failed to save to localStorage:', error);
        }
      }),
      catchError((error) => {
        console.error('UserService: Error updating profile on backend:', error);
        console.error('UserService: Error status:', error.status);
        console.error('UserService: Error message:', error.message);
        
        // Try to save to localStorage as fallback
        try {
          localStorage.setItem('userProfile', JSON.stringify(profile));
          console.log('UserService: Profile saved to localStorage as fallback');
          // Update the BehaviorSubject
          this.userProfileSubject.next(profile);
          // Return the profile as if it was saved successfully
          return of(profile);
        } catch (storageError) {
          console.error('UserService: Failed to save to localStorage:', storageError);
          // Re-throw the original error
          throw error;
        }
      })
    );
  }
}
