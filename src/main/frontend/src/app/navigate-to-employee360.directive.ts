import { Directive, Input, HostListener } from '@angular/core';
import { Employee360Service } from './services/employee360.service';
import { BreadcrumbService } from './services/breadcrumb.service';
import { Breadcrumb } from './models/breadcrumd';


@Directive({
  selector: '[navigateToEmployee360]',
})
export class NavigateToEmployee360Directive {
  @Input('navigateToEmployee360') data: any; 
  store:any;

  constructor(private employee360Service: Employee360Service,
    private breadcrumbService: BreadcrumbService) {}

  @HostListener('click') onClick() {
    if (this.data) {
     this.store=this.data;

      let breadcrumbObject = new Breadcrumb();
      breadcrumbObject.title = "Employee 360";
      breadcrumbObject.url = "/employee-360";
      breadcrumbObject.object=this.data;

       const stringifiedData = typeof this.store === 'string' ? this.store : JSON.stringify(this.store);
       localStorage.setItem('employee360Data', stringifiedData);
      this.employee360Service.navigateToEmployee360(this.data);
      this.breadcrumbService.addObjectToAddInBreadcrumb(breadcrumbObject);
    }
  }
}
