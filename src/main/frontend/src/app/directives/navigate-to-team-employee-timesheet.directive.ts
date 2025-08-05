import { Directive, HostListener, Input } from '@angular/core';
import { Router } from '@angular/router';

@Directive({
  selector: '[navigateToTeamEmployeeTimesheet]'
})
export class NavigateToTeamEmployeeTimesheetDirective {

  @Input('navigateToTeamEmployeeTimesheet') 
  projectId: any;

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
      const fullUrl = `${window.location.origin}/#${relativeUrl}`;

      window.open(fullUrl, '_blank'); 
    }
  }
}
