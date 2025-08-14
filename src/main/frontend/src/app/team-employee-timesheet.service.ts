import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class TeamEmployeeTimesheetService {

   private navigationSubject = new Subject<void>();

  constructor(private router: Router) { }

  getNavigationEvent() {
    return this.navigationSubject.asObservable();
  }

  navigateToTeamEmployeeTimesheet(projectId: any) {
    this.router.navigate(['/team-employee-timesheet'], { state: { projectId } }).then(() => {
      this.navigationSubject.next();
    });
  }
}
