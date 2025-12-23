import { Pipe, PipeTransform } from '@angular/core';
import * as moment from 'moment';

@Pipe({
  standalone: false,
  name: 'sort'
})
export class SortPipe implements PipeTransform {

  transform(value: Array<any>, args: any[]): any {
    const sortField = args[0];
    const sortFieldType = args[1];
    const sortDirection = args[2];
    let multiplier = 1;

    if (sortDirection === 'desc') {
      multiplier = -1;
    }

    //console.log("Sort Data : ", value);


    value.sort((a: any, b: any) => {
      if (sortFieldType == "string") {
        if ((a[sortField]+"")?.toLowerCase() < (b[sortField]+"")?.toLowerCase()) {
          return -1 * multiplier;
        } else if ((a[sortField]+"")?.toLowerCase() > (b[sortField]+"")?.toLowerCase()) {
          return 1 * multiplier;
        } else {
          return 0;
        }
      } else if (sortFieldType == "date") {
        if(!a[sortField]){
          return -1 * multiplier;
        }else if(!b[sortField]){
          return 1 * multiplier;
        }else{
          const timeA = moment(a[sortField], "DD-MM-YYYY");
          const timeB = moment(b[sortField], "DD-MM-YYYY");
  
          if (timeA.isBefore(timeB)) {
            return -1 * multiplier;
          } else if (timeA.isAfter(timeB)) {
            return 1 * multiplier;
          } else {
            return 0;
          }
        }
      } else if (sortFieldType == "datetime") {
        if(!a[sortField]){
          return -1 * multiplier;
        }else if(!b[sortField]){
          return 1 * multiplier;
        }else{
          const timeA = moment(a[sortField], "DD-MM-YYYY HH:mm:ss");
          const timeB = moment(b[sortField], "DD-MM-YYYY HH:mm:ss");
  
          if (timeA.isBefore(timeB)) {
            return -1 * multiplier;
          } else if (timeA.isAfter(timeB)) {
            return 1 * multiplier;
          } else {
            return 0;
          }
        }
      } else if (sortFieldType == "empId") {
        // For Employment ID Sorting
        if (a[sortField]?.startsWith("A-") && b[sortField]?.startsWith("A-")) {

          const empIdA = +a[sortField].substring(2);
          const empIdB = +b[sortField].substring(2);

          if (empIdA < empIdB) {
            return -1 * multiplier;
          } else if (empIdA > empIdB) {
            return 1 * multiplier;
          } else {
            return 0;
          }
        }
      } else if (sortFieldType == "number") {
        const numberA = +a[sortField];
        const numberB = +b[sortField];

        if (numberA < numberB) {
          return -1 * multiplier;
        } else if (numberA > numberB) {
          return 1 * multiplier;
        } else {
          return 0;
        }
      } else {
        if (a[sortField] < b[sortField]) {
          return -1 * multiplier;
        } else if (a[sortField] > b[sortField]) {
          return 1 * multiplier;
        } else {
          return 0;
        }
      }
    });

    return value;
  }

}
