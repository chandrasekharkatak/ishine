import { Component, Input, Output, EventEmitter, ViewChild, TemplateRef } from '@angular/core';
import { M } from "../../../../node_modules/@angular/material/form-field.d-CMA_QQ0R";
import { MatTimepickerModule } from "@angular/material/timepicker";
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';

/**
 * Timesheet Time Entry Component
 * 
 * Extracted component for handling office In/Out time, total working hours, and night shift selection.
 * 
 * Responsibilities:
 * - Display and manage office in/out time pickers
 * - Calculate and display total working hours
 * - Handle night shift checkbox with confirmation modal
 * - Communicate time changes back to parent
 */
@Component({
  selector: 'app-timesheet-time-entry',
  templateUrl: './timesheet-time-entry.component.html',
  styleUrls: ['./timesheet-time-entry.component.css'],
  imports: [ MatTimepickerModule,FormsModule,CommonModule, MatFormFieldModule,
    MatInputModule,MatIconModule]
})
export class TimesheetTimeEntryComponent {

  @ViewChild('officeInTimePicker') officeInTimePicker: any;
  @ViewChild('officeOutTimePicker') officeOutTimePicker: any;

  /**
   * Office In Time (two-way binding)
   */
  @Input() officeInTime: Date | null = null;

  /**
   * Office Out Time (two-way binding)
   */
  @Input() officeOutTime: Date | null = null;

  /**
   * Total Working Office Hours (display only)
   */
  @Input() totalWorkingOfficeHours: string = '00:00';

  /**
   * Night Shift flag
   */
  @Input() isNightShift: boolean = false;

  /**
   * Whether this is an update operation
   */
  @Input() isUpdation: boolean = false;

  /**
   * Date filter for in-time picker
   */
  @Input() dateFilter: (date: Date | null) => boolean;

  /**
   * Custom date filter for non-working days
   */
  @Input() customDateFilter: (date: Date | null) => boolean;

  /**
   * Day type to determine which filter to use
   */
  @Input() dayType: string = '';

  /**
   * Maximum date for out-time picker
   */
  @Input() maxOutTimeDate: Date;

  /**
   * Out time filter function
   */
  @Input() outTimeFilter: (date: Date | null) => boolean;

  /**
   * Alert template for night shift confirmation
   */
  @Input() nightShiftTemplate: TemplateRef<any>;

  @Input() aleartTemplate: TemplateRef<any>;

  /**
   * Event emitted when in-time changes
   */
  @Output() officeInTimeChange = new EventEmitter<Date | null>();

  /**
   * Event emitted when out-time changes
   */
  @Output() officeOutTimeChange = new EventEmitter<Date | null>();

  /**
   * Event emitted when night shift checkbox is clicked
   */
  @Output() nightShiftClick = new EventEmitter<{ event: Event, template: TemplateRef<any> }>();

  /**
   * Event emitted when night shift value changes
   */
  @Output() isNightShiftChange = new EventEmitter<boolean>();

  /**
   * Event emitted when manual date input should be disabled
   */
  @Output() disableManualInput = new EventEmitter<void>();

  /**
   * Event emitted when total working hours should be reset
   */
  @Output() resetTotalHours = new EventEmitter<TemplateRef<any>>();

  /**
   * Event emitted when total working hours should be calculated
   */
  @Output() calculateTotalHours = new EventEmitter<void>();

  /**
   * Handler for in-time change
   */
  onInTimeChange(date: Date | null): void {
    this.officeInTime = date;
    this.officeInTimeChange.emit(date);
    if (this.aleartTemplate) {
      this.resetTotalHours.emit(this.aleartTemplate);
    }
  }

  /**
   * Handler for out-time change
   */
  onOutTimeChange(date: Date | null): void {
    this.officeOutTime = date;
    this.officeOutTimeChange.emit(date);
    this.calculateTotalHours.emit();
  }

  /**
   * Handler for night shift checkbox click
   */
  onNightShiftClick(event: Event): void {
    if (this.nightShiftTemplate) {
      this.nightShiftClick.emit({ event, template: this.nightShiftTemplate });
    }
  }

  /**
   * Handler for night shift value change (after confirmation)
   */
  onNightShiftChange(value: boolean): void {
    this.isNightShift = value;
    this.isNightShiftChange.emit(value);
  }

  /**
   * Handler for manual input keydown
   */
  onKeyDown(): void {
    this.disableManualInput.emit();
  }

  /**
   * Get the appropriate date filter based on day type
   */
  getDateFilter(): (date: Date | null) => boolean {
    return (this.dayType === 'Non-working') ? this.customDateFilter : this.dateFilter;
  }
}

