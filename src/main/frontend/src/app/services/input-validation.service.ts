import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class InputValidationService {

  constructor() { }


  private maliciousPattern = /<script.*?>|<\/script>|<.*?>|javascript:|on\w+=/gi;

  validateInput(value: string, fieldName: string = 'Input'): string {
    if (this.maliciousPattern.test(value)) {
      alert(`${fieldName} contains invalid text and has been cleared.`);
      return ''; // clear invalid input
    }
    return value;
  }
}
