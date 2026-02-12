import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class EmployeeIdUtilService {

  constructor() { }

  extractNumericId(prefixedId: any): string | null {
    if (!prefixedId || typeof prefixedId !== 'string') {
      return null;
    }

    const parts = prefixedId.split('-');
    return parts.length === 2 && parts[1] ? parts[1] : null;
  }

  generateEmploymentId(
    id: any,
    isApmosysProduct: any,
    isConsultant: any
  ): string | null {

    if (!id) {
      return null;
    }

    const isApmosys = isApmosysProduct === 'true';
    const isCons = isConsultant === 'true';

    if (isApmosys && isCons) {
      return null;
    }

    if (isApmosys) {
      return `AP-${id}`;
    }

    if (isCons) {
      return `CS-${id}`;
    }

    return `A-${id}`;
  }

}
