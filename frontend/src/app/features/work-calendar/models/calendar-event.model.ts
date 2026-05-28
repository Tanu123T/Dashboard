export interface CalendarEvent {
  id: string;
  title: string;
  startTime: Date;
  endTime: Date;
  color: string;
  description?: string;
  location?: string;
  attendees?: string[];
}
