import { Directive, HostListener, Input } from '@angular/core';
import { TeamEmployeeTimesheetService } from '../team-employee-timesheet.service';
import { Router } from '@angular/router';

@Directive({
  selector: '[navigateToTeamEmployeeTimesheet]'
})
export class NavigateToTeamEmployeeTimesheetDirective {

  @Input('navigateToTeamEmployeeTimesheet') 
  projectId: any;

  constructor(private teamEmployeeTimesheetService: TeamEmployeeTimesheetService,
    private router: Router) { }

  @HostListener('click') onClick() {
      // console.log("data",this.data);
      if (this.projectId) {
        localStorage.setItem('projectId', this.projectId);
        this.router.navigate(['/team-employee-timesheet'], {
          queryParams: { projectId: this.projectId }
        });
        // this.teamEmployeeTimesheetService.navigateToTeamEmployeeTimesheet(this.projectId);
      }
    }
}
