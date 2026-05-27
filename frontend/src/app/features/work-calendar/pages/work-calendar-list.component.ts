import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { WorkCalendarService } from '../services/work-calendar.service';
import { CalendarEvent } from '../models/calendar-event.model';

@Component({
  selector: 'app-work-calendar-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './work-calendar-list.component.html',
  styleUrls: ['./work-calendar-list.component.css']
})
export class WorkCalendarListComponent implements OnInit {
  currentDate = new Date();
  selectedDate: Date | null = new Date();
  calendarDays: Date[] = [];
  selectedDayEvents: CalendarEvent[] = [];
  timeSlots = this.generateTimeSlots();
  allEvents: CalendarEvent[] = [];
  monthYear = '';
  viewType: 'grid' | 'list' = 'grid';
  
  // Modal properties
  showEventModal = false;
  selectedTimeSlot: { date: Date; hour: number } | null = null;
  editingEventId: string | null = null;
  eventForm = {
    title: '',
    startTime: '',
    endTime: '',
    category: 'Meeting',
    description: ''
  };

  constructor(private workCalendarService: WorkCalendarService) { }

  ngOnInit(): void {
    this.generateCalendarDays();
    this.updateMonthYear();
    this.workCalendarService.getEvents().subscribe(events => {
      this.allEvents = events;
      this.updateSelectedDayEvents();
    });
  }

  generateCalendarDays(): void {
    const year = this.currentDate.getFullYear();
    const month = this.currentDate.getMonth();
    const date = this.currentDate.getDate();
    
    const current = new Date(year, month, date);
    const dayOfWeek = current.getDay();
    
    // Go back to Sunday of this week
    const startDate = new Date(current);
    startDate.setDate(current.getDate() - dayOfWeek);

    this.calendarDays = [];
    
    // Get 7 days (one week)
    for (let i = 0; i < 7; i++) {
      const day = new Date(startDate);
      day.setDate(startDate.getDate() + i);
      this.calendarDays.push(day);
    }
  }

  generateTimeSlots(): string[] {
    const slots = [];
    for (let i = 9; i <= 18; i++) {
      const hour = i % 12 || 12;
      const period = i < 12 ? 'AM' : 'PM';
      slots.push(`${hour}:00 ${period}`);
    }
    return slots;
  }

  onDateSelect(date: Date): void {
    // Create a clean date copy
    const selectedDateCopy = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    this.selectedDate = selectedDateCopy;
    
    // Update the main calendar's current date to the selected date
    // This ensures the week containing the selected date is displayed
    this.currentDate = new Date(selectedDateCopy.getFullYear(), selectedDateCopy.getMonth(), selectedDateCopy.getDate());
    
    // Regenerate calendar days to show the week containing selected date
    this.generateCalendarDays();
    
    // Update month/year display in mini calendar
    this.updateMonthYear();
    
    // Update the selected day events sidebar
    this.updateSelectedDayEvents();
  }

  private updateSelectedDayEvents(): void {
    if (this.selectedDate) {
      this.selectedDayEvents = this.allEvents.filter(event =>
        event.startTime.toDateString() === this.selectedDate!.toDateString()
      );
    }
  }

  getEventsForTimeSlot(slotIndex: number, date: Date): CalendarEvent[] {
    const slotHour = 9 + slotIndex;
    const slotEnd = slotHour + 1;
    return this.allEvents.filter(event => {
      const isSameDay = event.startTime.toDateString() === date.toDateString();
      const eventStart = event.startTime.getHours();
      const eventMinutes = event.startTime.getMinutes();
      const startsInSlot = eventStart === slotHour || 
                          (eventStart < slotEnd && event.endTime.getTime() > new Date(date.getFullYear(), date.getMonth(), date.getDate(), slotHour).getTime());
      return isSameDay && startsInSlot;
    });
  }

  getEventDuration(event: CalendarEvent): number {
    const diff = event.endTime.getTime() - event.startTime.getTime();
    return Math.ceil(diff / (1000 * 60)); // minutes
  }

  getEventHeight(event: CalendarEvent): string {
    const minutes = this.getEventDuration(event);
    const slotHeightMinutes = 60;
    const heightInPixels = (minutes / slotHeightMinutes) * 50;
    return `${Math.max(heightInPixels, 40)}px`;
  }

  previousMonth(): void {
    this.currentDate.setMonth(this.currentDate.getMonth() - 1);
    this.currentDate = new Date(this.currentDate);
    this.generateCalendarDays();
    this.updateMonthYear();
    this.updateSelectedDayEvents();
  }

  nextMonth(): void {
    this.currentDate.setMonth(this.currentDate.getMonth() + 1);
    this.currentDate = new Date(this.currentDate);
    this.generateCalendarDays();
    this.updateMonthYear();
    this.updateSelectedDayEvents();
  }


  openEventModal(date: Date, hour: number): void {
    // Create a proper date copy to avoid reference issues
    const clickedDate = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    this.selectedTimeSlot = { date: clickedDate, hour };
    this.selectedDate = clickedDate; // Update selected date to reflect clicked slot
    
    // Format time properly for 12-hour format
    let startHour = hour;
    let startAmpm = 'AM';
    if (hour >= 12) {
      startAmpm = 'PM';
      if (hour > 12) startHour = hour - 12;
    }
    if (hour === 0) startHour = 12;
    
    let endHour = hour + 1;
    let endAmpm = 'AM';
    if (endHour >= 12) {
      endAmpm = 'PM';
      if (endHour > 12) endHour = endHour - 12;
    }
    if (endHour === 0) endHour = 12;
    
    this.eventForm = {
      title: '',
      startTime: `${String(startHour).padStart(2, '0')}:00 ${startAmpm}`,
      endTime: `${String(endHour).padStart(2, '0')}:00 ${endAmpm}`,
      category: 'Meeting',
      description: ''
    };
    this.showEventModal = true;
  }

  // Helper method to format date as YYYY-MM-DD for input[type="date"]
  formatDateForInput(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  // Helper method to get formatted date display for modal
  getFormattedSlotDate(): string {
    if (!this.selectedTimeSlot) return '';
    const date = this.selectedTimeSlot.date;
    const options: Intl.DateTimeFormatOptions = { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' };
    return date.toLocaleDateString('en-US', options);
  }

  editEvent(event: CalendarEvent): void {
    this.editingEventId = event.id;
    this.selectedTimeSlot = {
      date: new Date(event.startTime),
      hour: event.startTime.getHours()
    };
    
    this.eventForm = {
      title: event.title,
      startTime: event.startTime.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', hour12: true }),
      endTime: event.endTime.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', hour12: true }),
      category: this.getCategoryFromColor(event.color),
      description: event.description || ''
    };
    
    this.showEventModal = true;
  }

  closeEventModal(): void {
    this.showEventModal = false;
    this.selectedTimeSlot = null;
    this.editingEventId = null;
    this.eventForm = {
      title: '',
      startTime: '',
      endTime: '',
      category: 'Meeting',
      description: ''
    };
  }

  saveEvent(): void {
    if (!this.eventForm.title || !this.selectedTimeSlot) return;

    const slotDate = this.selectedTimeSlot.date;
    const startDate = new Date(slotDate.getFullYear(), slotDate.getMonth(), slotDate.getDate());
    const endDate = new Date(slotDate.getFullYear(), slotDate.getMonth(), slotDate.getDate());
    
    // Parse start time from formatted string (e.g., "09:00 AM")
    const startTimeParts = this.eventForm.startTime.match(/(\d+):(\d+)\s(AM|PM)/i);
    if (startTimeParts) {
      let startHour = parseInt(startTimeParts[1]);
      const startMinute = parseInt(startTimeParts[2]);
      const startPeriod = startTimeParts[3].toUpperCase();
      
      // Convert to 24-hour format
      if (startPeriod === 'PM' && startHour !== 12) startHour += 12;
      if (startPeriod === 'AM' && startHour === 12) startHour = 0;
      
      startDate.setHours(startHour, startMinute, 0);
    }

    // Parse end time from formatted string (e.g., "10:00 AM")
    const endTimeParts = this.eventForm.endTime.match(/(\d+):(\d+)\s(AM|PM)/i);
    if (endTimeParts) {
      let endHour = parseInt(endTimeParts[1]);
      const endMinute = parseInt(endTimeParts[2]);
      const endPeriod = endTimeParts[3].toUpperCase();
      
      // Convert to 24-hour format
      if (endPeriod === 'PM' && endHour !== 12) endHour += 12;
      if (endPeriod === 'AM' && endHour === 12) endHour = 0;
      
      endDate.setHours(endHour, endMinute, 0);
    }

    if (this.editingEventId) {
      // Update existing event
      const updatedEvent: CalendarEvent = {
        id: this.editingEventId,
        title: this.eventForm.title,
        description: this.eventForm.description,
        startTime: startDate,
        endTime: endDate,
        color: this.getCategoryColor(this.eventForm.category)
      };
      this.workCalendarService.updateEvent(this.editingEventId, updatedEvent);
    } else {
      // Create new event
      const newEvent: CalendarEvent = {
        id: Date.now().toString(),
        title: this.eventForm.title,
        description: this.eventForm.description,
        startTime: startDate,
        endTime: endDate,
        color: this.getCategoryColor(this.eventForm.category)
      };
      this.workCalendarService.addEvent(newEvent);
    }

    this.closeEventModal();
  }

  getCategoryColor(category: string): string {
    const colors: { [key: string]: string } = {
      'Meeting': '#4b8fe7',
      'Conference': '#2fc7a6',
      'Workshop': '#f59e0b',
      'Training': '#8b5cf6',
      'Other': '#6b7280'
    };
    return colors[category] || '#4b8fe7';
  }

  getCategoryFromColor(color: string): string {
    const colorMap: { [key: string]: string } = {
      '#4b8fe7': 'Meeting',
      '#2fc7a6': 'Conference',
      '#f59e0b': 'Workshop',
      '#8b5cf6': 'Training',
      '#6b7280': 'Other'
    };
    return colorMap[color] || 'Meeting';
  }

  goToToday(): void {
    this.currentDate = new Date();
    this.selectedDate = new Date();
    this.generateCalendarDays();
    this.updateMonthYear();
    this.updateSelectedDayEvents();
  }

  setViewType(type: 'grid' | 'list'): void {
    this.viewType = type;
  }

  private updateMonthYear(): void {
    const options: Intl.DateTimeFormatOptions = { year: 'numeric', month: 'long' };
    this.monthYear = this.currentDate.toLocaleDateString('en-US', options);
  }

  getMinicalendarDays(): Date[][] {
    const year = this.currentDate.getFullYear();
    const month = this.currentDate.getMonth();
    
    // Get first day of month
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const daysInMonth = lastDay.getDate();
    const startingDayOfWeek = firstDay.getDay();
    
    const weeks: Date[][] = [];
    let currentWeek: Date[] = [];
    
    // Add empty cells for days before month starts
    for (let i = 0; i < startingDayOfWeek; i++) {
      const prevDate = new Date(year, month, -(startingDayOfWeek - i - 1));
      currentWeek.push(prevDate);
    }
    
    // Add days of month
    for (let i = 1; i <= daysInMonth; i++) {
      currentWeek.push(new Date(year, month, i));
      if (currentWeek.length === 7) {
        weeks.push(currentWeek);
        currentWeek = [];
      }
    }
    
    // Add remaining cells
    if (currentWeek.length > 0) {
      for (let i = 1; currentWeek.length < 7; i++) {
        currentWeek.push(new Date(year, month + 1, i));
      }
      weeks.push(currentWeek);
    }
    
    return weeks;
  }

  isCurrentMonth(date: Date): boolean {
    return date.getMonth() === this.currentDate.getMonth();
  }

  isSelectedDate(date: Date): boolean {
    return this.selectedDate ? date.toDateString() === this.selectedDate.toDateString() : false;
  }

  isToday(date: Date): boolean {
    const today = new Date();
    return date.toDateString() === today.toDateString();
  }

  isTodayMinical(date: Date): boolean {
    const today = new Date();
    return date.toDateString() === today.toDateString();
  }

  hasEventsOnDate(date: Date): boolean {
    return this.allEvents.some(event => 
      event.startTime.toDateString() === date.toDateString()
    );
  }

  isOtherMonthMinical(date: Date): boolean {
    return date.getMonth() !== this.currentDate.getMonth();
  }

  deleteEvent(eventId: string): void {
    this.workCalendarService.deleteEvent(eventId);
    // Remove from local allEvents array
    this.allEvents = this.allEvents.filter(event => event.id !== eventId);
    this.updateSelectedDayEvents();
  }
}
