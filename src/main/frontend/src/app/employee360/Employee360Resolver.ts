import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot, Resolve } from '@angular/router';
import { Observable, of, from } from 'rxjs';
import { tap } from 'rxjs/operators';
import { UtilityService } from '../services/utility.service';
import { EncryptionService } from '../services/EncryptionService';

@Injectable({
  providedIn: 'root',
})
export class Employee360Resolver implements Resolve<any> {
  
  constructor(private utility: UtilityService,
        private encryptionService: EncryptionService
  ) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<any> {
    const employeeId = route.paramMap.get('id');
    const employee360Data = sessionStorage.getItem('employee360Data');

    if (employee360Data) {
      return of(JSON.parse(employee360Data)); // Return cached data
    } else {
      return from(this.utility.getEmployeeDetailsFor360ViewNewImple(employeeId)).pipe(
        tap((data) => {
          sessionStorage.setItem('employee360Data', JSON.stringify(data)); // Store fetched data
         
        })
      );
    
    }
   
  }
}
