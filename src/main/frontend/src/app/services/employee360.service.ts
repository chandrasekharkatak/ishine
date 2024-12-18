import { Injectable } from '@angular/core';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class Employee360Service {

  constructor(private router: Router) {}

  // Navigate to the target route with data
  navigateToEmployee360(data: any) {
    this.router.navigate(['/employee-360'], { state: { data } });
  }
}
