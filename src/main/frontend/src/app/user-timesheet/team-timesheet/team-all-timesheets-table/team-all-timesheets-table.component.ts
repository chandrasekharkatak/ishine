import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, NgModule, Output, SimpleChanges } from '@angular/core';
import { FormsModule, NgModel } from '@angular/forms';
import { Sort } from '@angular/material/sort';
import { DomSanitizer } from '@angular/platform-browser';

/**
 * TeamAllTimesheetsTableComponent
 *
 * Presentational component responsible for rendering the
 * \"Teams All Timesheets\" table in TeamTimesheetComponent.
 *
 * NOTE:
 * - This component is dumb: it only receives data via Inputs and
 *   emits user actions via Outputs.
 * - All business logic (sorting, filtering, navigation, revoke, etc.)
 *   remains in TeamTimesheetComponent.
 */
@Component({
  selector: 'app-team-all-timesheets-table',
  templateUrl: './team-all-timesheets-table.component.html',
  standalone:false,
  styleUrls: ['./team-all-timesheets-table.component.css']
})
export class TeamAllTimesheetsTableComponent {

  @Input() allTeamTimesheets: any[] = [];
  @Input() filters: any = {};
  @Input() isSearchEnabled = false;
  @Input() allTimesheetColumns: any[] = [];
  @Input() sortColumn: any;
  @Input() sortColumnType: any;
  @Input() sortDirection: string = 'asc';
  @Input() page = 1;
  @Input() userMapping: any = {};
  @Input() isSelectAll = false;
  @Input() bulkApprove: any[] = [];
  @Input() bulkReject: any[] = [];

  /**
   * Emits when sort is changed.
   */
  @Output() sortChange = new EventEmitter<Sort>();

  /**
   * Emits when search filters change.
   */
  @Output() searchChange = new EventEmitter<any>();

  /**
   * Emits when page changes.
   */
  @Output() pageChange = new EventEmitter<number>();

  /**
   * Emits when \"view\" is clicked for a timesheet.
   */
  @Output() viewDetails = new EventEmitter<any>();

  /**
   * Emits when \"revoke\" is clicked for a timesheet.
   */
  @Output() revoke = new EventEmitter<any>();

  @Output() reject = new EventEmitter<any>();
  @Output() previewDocument = new EventEmitter<any>();
  @Output() selectAllChange = new EventEmitter<any>();
  @Output() selectChange = new EventEmitter<any>();
  @Output() employee360Click = new EventEmitter<void>();

  /***/

  // onSortChange(sort: Sort): void {
  //   this.sortChange.emit(sort);
  // }

  // Local state
  localBulkApprove: any[] = [];
  localBulkReject: any[] = [];
  localIsSelectAll = false;

  constructor(private sanitizer: DomSanitizer) {}

  ngOnChanges(changes: SimpleChanges): void {
    // Sync local bulk approve array with parent input
    if (changes['bulkApprove']) {
      this.localBulkApprove = [...this.bulkApprove];
    }
    
    // Sync local bulk reject array with parent input
    if (changes['bulkReject']) {
      this.localBulkReject = [...this.bulkReject];
    }
    
    // Sync select all checkbox state with parent input
    if (changes['isSelectAll']) {
      this.localIsSelectAll = this.isSelectAll;
    }
  }

  sortData(sort: Sort) {
    //console.log(sort);
    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
      const sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';

       // Create new sort object with parsed parameters
       const parsedSort: Sort = {
        active: this.sortColumn,
        direction: sortDirection
      };
      
      // Emit sort change to parent component
      this.sortChange.emit(parsedSort);
    }

  }


  onSearchChange(filters: any): void {
    this.searchChange.emit(filters);
  }

  onPageChange(page: number): void {
    this.pageChange.emit(page);
  }

  onView(timesheet: any): void {
    this.viewDetails.emit(timesheet);
  }

  onRevoke(timesheet: any): void {
    this.revoke.emit(timesheet);
  }

  onReject(timesheet: any): void {
    this.reject.emit(timesheet);
  }

  onPreviewDocument(documentId: any): void {
    this.previewDocument.emit(documentId);
  }
  onEmployee360Click(): void {
    this.employee360Click.emit();
  }

  selectAllTimesheet(event){
    this.bulkApprove = [];
   this.bulkReject = [];

   const checkboxes = document.querySelectorAll('.timesheet-req-checkbox');
   checkboxes.forEach((checkbox: any) => {
     //console.log("checkbox : ", checkbox);
     let checkboxIndex = checkbox.getAttribute('id');
     let checkedTimesheet = this.allTeamTimesheets.find((_timesheet, index) => _timesheet.checkId == checkboxIndex);

     if (event.target.checked) {
       checkbox.checked = true;
       this.bulkApprove.push(checkedTimesheet);
       this.bulkReject.push(checkedTimesheet);
     } else {
       checkbox.checked = false;
       this.bulkApprove.forEach((timesheet, index) => {
         if (timesheet == checkedTimesheet) this.bulkApprove.splice(index, 1);
       });
       this.bulkReject.forEach((timesheet, index) => {
         if (timesheet == checkedTimesheet) this.bulkReject.splice(index, 1);
       });
     }
   });
 }



}


