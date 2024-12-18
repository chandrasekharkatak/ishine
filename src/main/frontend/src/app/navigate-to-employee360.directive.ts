import { Directive, Input, HostListener } from '@angular/core';
import { Employee360Service } from './services/employee360.service';


@Directive({
  selector: '[navigateToEmployee360]',
})
export class NavigateToEmployee360Directive {
  @Input('navigateToEmployee360') data: any; 

  constructor(private employee360Service: Employee360Service) {}

  @HostListener('click') onClick() {
    if (this.data) {
      this.employee360Service.navigateToEmployee360(this.data);
    }
  }
}
