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
        const parseDate = (row: any): moment.Moment | null => {
          const v = row[sortField];
          if (v == null || v === '') return null;
          if (v instanceof Date) {
            const m = moment(v);
            return m.isValid() ? m : null;
          }
          const m = moment(v);
          if (m.isValid()) return m;
          const strict = moment(v, "DD-MM-YYYY", true);
          return strict.isValid() ? strict : null;
        };
        const timeA = parseDate(a);
        const timeB = parseDate(b);
        if (!timeA && !timeB) return 0;
        if (!timeA) return -1 * multiplier;
        if (!timeB) return 1 * multiplier;
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
        const empIdA = SortPipe.numericFromPrefixedId(sa);
        const empIdB = SortPipe.numericFromPrefixedId(sb);
        if (empIdA < empIdB) return -1 * multiplier;
        if (empIdA > empIdB) return 1 * multiplier;
        const va = String(sa).toLowerCase();
        const vb = String(sb).toLowerCase();
        if (va < vb) return -1 * multiplier;
        if (va > vb) return 1 * multiplier;
        return 0;
      } else if (sortFieldType == "number") {
        const pickNum = (row: any): number => {
          let v = row[sortField];
          if ((v == null || v === '') && sortField === 'calculatedExperience') {
            v = row['totalExperience'];
          }
          if (v == null || v === '') return 0;
          if (typeof v === 'number' && Number.isFinite(v)) return v;
          const n = parseFloat(String(v).replace(/,/g, ''));
          return Number.isFinite(n) ? n : 0;
        };
        const numberA = pickNum(a);
        const numberB = pickNum(b);
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

  private static numericFromPrefixedId(id: any): number {
    const s = String(id ?? '');
    if (s.startsWith('APCS-')) {
      return +s.substring(5) || 0;
    }
    if (s.startsWith('AP-')) {
      return +s.substring(3) || 0;
    }
    if (s.startsWith('CS-')) {
      return +s.substring(3) || 0;
    }
    if (s.startsWith('A-')) {
      return +s.substring(2) || 0;
    }
    return +s || 0;
  }

}
