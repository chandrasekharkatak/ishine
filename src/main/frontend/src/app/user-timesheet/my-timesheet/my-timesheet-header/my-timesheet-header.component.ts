import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';

/**
 * MyTimesheetHeaderComponent
 *
 * Presentational header for MyTimesheetComponent.
 * Renders the action buttons:
 * - Create New Timesheet
 * - Bulk Final Document Upload
 * - View My Timesheets
 *
 * NOTE:
 * - This component is deliberately dumb: it exposes Inputs for state
 *   and emits Outputs for user actions.
 * - All business logic remains in the parent MyTimesheetComponent.
 */
@Component({
  selector: 'app-my-timesheet-header',
  imports: [
    CommonModule,
    FormsModule 
  ],
  templateUrl: './my-timesheet-header.component.html',
  styleUrls: ['./my-timesheet-header.component.css']
})
export class MyTimesheetHeaderComponent {

  /**
   * Role-based feature mapping from parent (used to control button visibility).
   */
  @Input() userMapping: any = {};

  /**
   * Whether the create timesheet form is currently active.
   */
  @Input() isTimesheetForm = false;

  /**
   * Whether the bulk upload form is currently active.
   */
  @Input() isTimesheetBulkForm = false;

  /**
   * Whether the self timesheet table view is currently active.
   */
  @Input() isTimesheetTable = false;

  /**
   * Whether client-side ID is needed (controls Bulk Upload button visibility).
   */
  @Input() clientIdNeeded = false;

  /**
   * Emitted when user clicks "Create New Timesheet".
   */
  @Output() createTimesheetClicked = new EventEmitter<void>();

  /**
   * Emitted when user clicks "Bulk Final Document Upload".
   */
  @Output() bulkUploadClicked = new EventEmitter<void>();

  /**
   * Emitted when user clicks "View My Timesheets".
   */
  @Output() viewMyTimesheetsClicked = new EventEmitter<void>();

  onCreateClick(): void {
    this.createTimesheetClicked.emit();
  }

  onBulkUploadClick(): void {
    this.bulkUploadClicked.emit();
  }

  onViewMyTimesheetsClick(): void {
    this.viewMyTimesheetsClicked.emit();
  }
}


