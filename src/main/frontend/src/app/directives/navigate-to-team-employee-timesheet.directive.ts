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
    poProjectId: this.poProjectId
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

    const urlTree = this.router.createUrlTree(['/team-employee-timesheet'], { queryParams });
    const relativeUrl = this.router.serializeUrl(urlTree);
    const fullUrl = `${window.location.origin}${window.location.pathname}#${relativeUrl}`;

    console.log('Final URL:', fullUrl); // ✅ This will confirm what’s actually being opened

    window.open(fullUrl, '_blank');
  }
}
}
