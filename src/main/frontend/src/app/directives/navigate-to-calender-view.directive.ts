import { Directive, HostListener, Input } from '@angular/core';
import { Router } from '@angular/router';

@Directive({
  selector: '[appNavigateToCalenderView]'
})
export class NavigateToCalenderViewDirective {

  @Input('appNavigateToCalenderView') projectId: any;
  @Input() empId: any;
  @Input() formattedMonthLabel: any;

  constructor(
    private router: Router) 
  { }

  @HostListener('click')
  onClick() {
    if (this.projectId && this.empId) {
      const urlTree = this.router.createUrlTree(['/calendar-view'], {
        queryParams: {
          projectId: this.projectId,
          empId: this.empId,
          formattedMonthLabel: this.formattedMonthLabel
        }
      });

      console.log("Navigating to Calendar View with params:", {
        projectId: this.projectId,
        empId: this.empId,
        formattedMonthLabel: this.formattedMonthLabel
      });

      const relativeUrl = this.router.serializeUrl(urlTree);
      const fullUrl = `${window.location.origin}/#${relativeUrl}`;
      window.open(fullUrl, '_blank');
    }
  }
}