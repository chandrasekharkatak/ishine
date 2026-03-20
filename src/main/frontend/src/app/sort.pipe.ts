import { Pipe, PipeTransform } from '@angular/core';
import * as moment from 'moment';

@Pipe({
  standalone: false,
  name: 'sort'
})
export class SortPipe implements PipeTransform {

  transform(value: Array<any>, args: any[]): any {
    if (!value || !Array.isArray(value)) return value;
    if (!args || args.length < 3) return value;
    const sortField = args[0];
    const sortFieldType = args[1];
    const sortDirection = args[2];
    if (sortField == null || sortField === '' || sortDirection == null || sortDirection === '') {
      return value;
    }
    let multiplier = 1;
    if (sortDirection === 'desc') {
      multiplier = -1;
    }
    const valueCopy = [...value];
    valueCopy.sort((a: any, b: any) => {
      if (sortFieldType == "string") {
        const va = (a[sortField] + "")?.toLowerCase() ?? '';
        const vb = (b[sortField] + "")?.toLowerCase() ?? '';
        if (va < vb) return -1 * multiplier;
        if (va > vb) return 1 * multiplier;
        return 0;
      } else if (sortFieldType == "date") {
        if (!a[sortField]) return -1 * multiplier;
        if (!b[sortField]) return 1 * multiplier;
        const timeA = moment(a[sortField], "DD-MM-YYYY");
        const timeB = moment(b[sortField], "DD-MM-YYYY");
        if (timeA.isBefore(timeB)) return -1 * multiplier;
        if (timeA.isAfter(timeB)) return 1 * multiplier;
        return 0;
      } else if (sortFieldType == "datetime") {
        if (!a[sortField]) return -1 * multiplier;
        if (!b[sortField]) return 1 * multiplier;
        const timeA = moment(a[sortField], "DD-MM-YYYY HH:mm:ss");
        const timeB = moment(b[sortField], "DD-MM-YYYY HH:mm:ss");
        if (timeA.isBefore(timeB)) return -1 * multiplier;
        if (timeA.isAfter(timeB)) return 1 * multiplier;
        return 0;
      } else if (sortFieldType == "empId") {
        const sa = a[sortField] ?? '';
        const sb = b[sortField] ?? '';
        if (typeof sa === 'string' && sa.startsWith("A-") && typeof sb === 'string' && sb.startsWith("A-")) {
          const empIdA = +sa.substring(2) || 0;
          const empIdB = +sb.substring(2) || 0;
          if (empIdA < empIdB) return -1 * multiplier;
          if (empIdA > empIdB) return 1 * multiplier;
        }
        const va = String(sa).toLowerCase();
        const vb = String(sb).toLowerCase();
        if (va < vb) return -1 * multiplier;
        if (va > vb) return 1 * multiplier;
        return 0;
      } else if (sortFieldType == "number") {
        const numberA = +a[sortField] || 0;
        const numberB = +b[sortField] || 0;
        if (numberA < numberB) return -1 * multiplier;
        if (numberA > numberB) return 1 * multiplier;
        return 0;
      } else {
        const va = a[sortField];
        const vb = b[sortField];
        if (va < vb) return -1 * multiplier;
        if (va > vb) return 1 * multiplier;
        return 0;
      }
    });
    return valueCopy;
  }

}
