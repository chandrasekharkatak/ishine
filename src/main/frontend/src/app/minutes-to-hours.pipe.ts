import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'minutesToHours'
})
export class MinutesToHoursPipe implements PipeTransform {
  transform(value: number): string {
    if (value === null || value === undefined) {
      return 'NA'; 
    }

    const hours = Math.floor(value / 60);
    const mins = value % 60;
    return `${hours} hour${hours !== 1 ? 's' : ''} ${mins} min${mins !== 1 ? 's' : ''}`;
  }
}