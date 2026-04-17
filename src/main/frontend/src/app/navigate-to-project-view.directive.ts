import { Directive, Input, HostListener } from '@angular/core';
import { Router } from '@angular/router';

@Directive({
  standalone: false,
  selector: '[navigateToProjectView]'
})
export class NavigateToProjectViewDirective {

  @Input('navigateToProjectView') 
  data: any; 

  constructor(private router: Router) { }

  @HostListener('click') onClick() {
    if (this.data) {
      const urlTree = this.router.createUrlTree(['/project-view'], {
        queryParams: { projectId: this.data }
      });
      const serializedUrl = this.router.serializeUrl(urlTree);
      const base = `${window.location.origin}${window.location.pathname}`;
      const fullUrl = `${base}#${serializedUrl}`;
      window.open(fullUrl, '_blank');
    }
  }
}
