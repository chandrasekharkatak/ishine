import { Directive, HostListener, Input } from '@angular/core';
import { Router } from '@angular/router';
import { environment } from 'src/environments/environment';

@Directive({
  standalone: false,
  selector: '[navigateToTeamEmployeeTimesheet]'
})
export class NavigateToTeamEmployeeTimesheetDirective {

  @Input('navigateToTeamEmployeeTimesheet') 
  projectId: any; 
  @Input() formattedMonthLabel!: string;
  @Input() isClientDashboard!: boolean;
  @Input() poProjectId: any;
  @Input() fromDate: any;
  @Input() toDate: any;
  @Input() poNo: string;
  private baseUrl: any = environment.baseUrl;
  private baseUrl360: any = environment.baseUrl360;

  constructor(
    private router: Router)   
  { }

  @HostListener('click')
onClick() {
  console.log('Directive triggered with values:', {
    projectId: this.projectId,
    formattedMonthLabel: this.formattedMonthLabel,
    isClientDashboard: this.isClientDashboard,
    poProjectId: this.poProjectId,
    fromDate: this.fromDate,
    toDate: this.toDate,
    poNo: this.poNo
  });

  if (this.projectId) {
    const queryParams: any = { projectId: this.projectId };

    if (this.formattedMonthLabel) {
      queryParams.formattedMonthLabel = this.formattedMonthLabel;
    }

    if (this.isClientDashboard !== undefined) {
      queryParams.isClientDashboard = this.isClientDashboard;
    }

    if (this.poProjectId !== undefined && this.poProjectId !== null && this.poProjectId !== '') {
      queryParams.poProjectId = this.poProjectId;
    }

    const normalizedFromDate = this.normalizeDateForQuery(this.fromDate);
    const normalizedToDate = this.normalizeDateForQuery(this.toDate);
    if (normalizedFromDate) {
      queryParams.fromDate = normalizedFromDate;
    }
    if (normalizedToDate) {
      queryParams.toDate = normalizedToDate;
    }
    if (this.poNo !== undefined && this.poNo !== null && this.poNo.toString().trim() !== '') {
      queryParams.poNo = this.poNo.toString().trim();
    }

    const urlTree = this.router.createUrlTree(['/team-employee-timesheet'], { queryParams });
    const relativeUrl = this.router.serializeUrl(urlTree);
    const fullUrl = `${window.location.origin}${window.location.pathname}#${relativeUrl}`;

    console.log('Final URL:', fullUrl); // ✅ This will confirm what’s actually being opened

    window.open(fullUrl, '_blank');
  }
}

private normalizeDateForQuery(value: any): string | null {
  if (!value) return null;
  const date = new Date(value);
  if (isNaN(date.getTime())) return null;
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${date.getFullYear()}-${month}-${day}`;
}
}
