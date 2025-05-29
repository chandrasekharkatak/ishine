import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'filterEmployee'
})
export class FilterEmployeePipe implements PipeTransform {
 
  transform(employees: any[], searchTerm: string): any[] {
    if (!employees || !searchTerm) {
      return employees;
    }

    searchTerm = searchTerm.toLowerCase();
    return employees.filter(emp =>
      (emp.name && emp.name.toLowerCase().includes(searchTerm)) ||
      (emp.employmentId && emp.employmentId.toLowerCase().includes(searchTerm))
    );
  }

}
