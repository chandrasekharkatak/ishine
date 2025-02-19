import { Directive, Input, HostListener } from '@angular/core';
import { Employee360Service } from './services/employee360.service';
import { ProjectViewService } from './services/project-view.service';

@Directive({
  selector: '[navigateToProjectView]'
})
export class NavigateToProjectViewDirective {

  @Input('navigateToProjectView') 
  data: any; 

  constructor(private projectViewService: ProjectViewService) { }

  @HostListener('click') onClick() {
    if (this.data) {
     
      localStorage.setItem('projectId', this.data);
      this.projectViewService.navigateToProjectView(this.data);
    }
  }
}
