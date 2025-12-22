import { Directive, Input, HostListener } from '@angular/core';
import { Employee360Service } from './services/employee360.service';
import { BreadcrumbService } from './services/breadcrumb.service';
import { Breadcrumb } from './models/breadcrumd';
import { UtilityService } from './services/utility.service';
import * as CryptoJS from 'crypto-js';
import { EncryptionService } from './services/EncryptionService';

@Directive({
  standalone: false,
  selector: '[navigateToEmployee360]',
})
export class NavigateToEmployee360Directive {

  allEmployeeList360: any[] = [];
  employeesFor360: any[] = [];
  @Input('navigateToEmployee360') data: any; 
  //@Input('navigateToEmployee360') newdata: any; 
  store:any;

  constructor(private employee360Service: Employee360Service,
    private breadcrumbService: BreadcrumbService,
    private encryptionService: EncryptionService,
  private utilityService:UtilityService) {

    }

    @HostListener('click') 
    onClick() {
    
     sessionStorage.removeItem("employee360Data");
     this.employee360Service.navigateToEmployee360(this.data);
     
    }
    
    private openNavigationInNewWindow(employeeData: any) {
      //alert(employeeData.empId);
      // Construct the URL for Employee360 page (you should replace this URL with the correct one for your app)
      const url = `/employee360/${employeeData.empId}`;  // Example URL, adjust as needed
      
      // Open a new window and navigate to the Employee360 page
      const newWindow = window.open(url, '_blank');
      
      // Optionally, you can send the data via URL params, postMessage, or via localStorage if needed
      if (newWindow) {
        let encryptedData = this.encryptionService.encrypt(JSON.stringify(employeeData));
        newWindow.sessionStorage.setItem('employee360Data', encryptedData);
      } else {
        console.error('Failed to open a new window');
      }
    }

}
