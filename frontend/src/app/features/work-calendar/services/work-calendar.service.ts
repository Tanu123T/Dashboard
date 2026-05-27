import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { CalendarEvent } from '../models/calendar-event.model';

@Injectable({
  providedIn: 'root'
})
export class WorkCalendarService {
  private readonly STORAGE_KEY = 'calendar_events';
  private events$ = new BehaviorSubject<CalendarEvent[]>([]);

  constructor() {
    this.loadEvents();
  }

  private loadEvents(): void {
    try {
      const stored = localStorage.getItem(this.STORAGE_KEY);
      if (stored) {
        const events = JSON.parse(stored);
        // Convert date strings back to Date objects
        const parsedEvents = events.map((event: any) => ({
          ...event,
          startTime: new Date(event.startTime),
          endTime: new Date(event.endTime)
        }));
        this.events$.next(parsedEvents);
      } else {
        // Load default events only if localStorage is empty
        const defaultEvents = [
          {
            id: '1',
            title: '1:1 with Kevin',
            startTime: new Date(2026, 4, 12, 9, 0),
            endTime: new Date(2026, 4, 12, 9, 30),
            color: '#17b8c4',
            description: 'One-on-one meeting with Kevin'
          },
          {
            id: '2',
            title: '1:1 with Savannah',
            startTime: new Date(2026, 4, 12, 9, 30),
            endTime: new Date(2026, 4, 12, 10, 0),
            color: '#17b8c4',
            description: 'One-on-one meeting with Savannah'
          },
          {
            id: '3',
            title: 'Demo Schedule',
            startTime: new Date(2026, 4, 12, 10, 0),
            endTime: new Date(2026, 4, 12, 11, 0),
            color: '#0369a1',
            description: 'Product demo session'
          }
        ];
        this.events$.next(defaultEvents);
        this.saveEvents(defaultEvents);
      }
    } catch (error) {
      console.error('Error loading events from localStorage:', error);
      this.events$.next([]);
    }
  }

  private saveEvents(events: CalendarEvent[]): void {
    try {
      localStorage.setItem(this.STORAGE_KEY, JSON.stringify(events));
    } catch (error) {
      console.error('Error saving events to localStorage:', error);
    }
  }

  getEvents(): Observable<CalendarEvent[]> {
    return this.events$.asObservable();
  }

  addEvent(event: CalendarEvent): void {
    const currentEvents = this.events$.value;
    const updatedEvents = [...currentEvents, event];
    this.events$.next(updatedEvents);
    this.saveEvents(updatedEvents);
  }

  updateEvent(id: string, updatedEvent: CalendarEvent): void {
    const currentEvents = this.events$.value;
    const updatedEvents = currentEvents.map(event =>
      event.id === id ? updatedEvent : event
    );
    this.events$.next(updatedEvents);
    this.saveEvents(updatedEvents);
  }

  deleteEvent(id: string): void {
    const currentEvents = this.events$.value;
    const filteredEvents = currentEvents.filter(event => event.id !== id);
    this.events$.next(filteredEvents);
    this.saveEvents(filteredEvents);
  }

  getEventById(id: string): CalendarEvent | undefined {
    return this.events$.value.find(event => event.id === id);
  }
}
