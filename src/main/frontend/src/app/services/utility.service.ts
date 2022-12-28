import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class UtilityService {

  constructor() { }

  appendEmployeementid(emp):string{
   return "A-".concat(emp);
 }

  substringEmployeementid(emp):string{
    if(emp.startsWith("A-")){
      return emp.substring(2);
    }else {
      console.error("invalid data found")
    }
}

}
