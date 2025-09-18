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
     let encryptedEmployeeData = sessionStorage.getItem('employee360Data');
            let employeeData = null;
            if (encryptedEmployeeData) {
                const decryptedString = this.encryptionService.decrypt(encryptedEmployeeData);
                if (decryptedString) {
                    try {
                        employeeData = JSON.parse(decryptedString);
                    } catch (error) {
                        console.error('Failed to parse decrypted session user:', decryptedString, error);
                        employeeData = null;
                    }
                } else {
                    console.warn('Decryption returned empty string.');
                    employeeData = null;
                }
            } else {
                console.warn('No currentUser found in sessionStorage');
                employeeData = null;
            }
            
    const employee360Data = employeeData;

    if (employee360Data) {
      return of(employee360Data); // Return cached data
    } else {
      return from(this.utility.getEmployeeDetailsFor360ViewNewImple(employeeId)).pipe(
        tap((data) => {
          let encryptedData = this.encryptionService.encrypt(JSON.stringify(data));
          sessionStorage.setItem('employee360Data', encryptedData); // Store fetched data
         
        })
      );
    
    }
   
  }
}
