import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';

/**
 * MyTimesheetApplicationSelectorComponent
 *
 * Presentational component responsible for:
 * - Selecting timesheet application type (Self / As Shadow / Team member)
 * - Selecting team member when timesheet is for team
 *
 * NOTE:
 * - This component is dumb: it exposes Inputs for current state
 *   and emits Outputs for user actions.
 * - All business logic (resetting forms, fetching metadata, toggling shadow)
 *   remains inside MyTimesheetComponent.
 */
@Component({
  standalone: false,
  selector: 'app-my-timesheet-application-selector',
  templateUrl: './my-timesheet-application-selector.component.html',
  styleUrls: ['./my-timesheet-application-selector.component.css']
})
export class MyTimesheetApplicationSelectorComponent {
getClientSideIdByProjectIdAndEmpId(arg0: any,arg1: any) {
throw new Error('Method not implemented.');
}

  /**
   * Current application type: 'self' | 'team'
   * Note: 'asShadow' has been moved to project-level toggle in timesheet-form component
   */
  @Input() timesheetAppliedFor: string;

  /**
   * Whether the timesheet is in update mode (disables radio & select when true).
   */
  @Input() isUpdation = false;

  /**
   * Current selected employee id for team member selection.
   */
  @Input() empId: any;

  /**
   * Currently logged-in user's empId (used to disable self in team list).
   */
  @Input() currentUserEmpId: any;

  /**
   * Team member list used in dropdown.
   */
  @Input() teamMemberList: any[] = [];

  /**
   * Emitted when application type changes (value: 'self' | 'team').
   * Parent will handle all side-effects.
   * Note: Shadow timesheet is now handled per-project in timesheet-form component.
   */
  @Output() applicationTypeChange = new EventEmitter<string>();

  /**
   * Emitted when team member selection changes.
   * For compatibility, we emit the raw event.target element so the parent
   * can call getTimesheetMetadata($event) as before.
   */
  @Output() teamMemberChange = new EventEmitter<any>();
shadowEmpId: any;
employeeList: any;
projectId: any;

  onApplicationTypeChange(value: string): void {
    this.applicationTypeChange.emit(value);
  }

  onTeamMemberChange(target: any): void {
    this.teamMemberChange.emit(target);
  }
}


