import { Directive, HostListener, Input } from '@angular/core';
import { Router } from '@angular/router';
import { environment } from 'src/environments/environment';

@Directive({
  selector: '[navigateToTeamEmployeeTimesheet]'
})
export class NavigateToTeamEmployeeTimesheetDirective {

  @Input('navigateToTeamEmployeeTimesheet') 
  projectId: any;
  private baseUrl: any = environment.baseUrl;
  private baseUrl360: any = environment.baseUrl360;

  constructor(
    private router: Router)   
  { }

  @HostListener('click')
  onClick() {
    if (this.projectId) {
      const urlTree = this.router.createUrlTree(['/team-employee-timesheet'], {
        queryParams: { projectId: this.projectId }
      });

      const relativeUrl = this.router.serializeUrl(urlTree);
      const fullUrl = `${window.location.origin}${window.location.pathname}#${relativeUrl}`;

      window.open(fullUrl, '_blank'); 
    }
  }
}
