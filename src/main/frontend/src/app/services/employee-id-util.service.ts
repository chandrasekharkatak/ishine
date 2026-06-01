import { Injectable } from '@angular/core';

export type EmployeeIdCategory = 'A' | 'CS' | 'AP' | 'APCS';

@Injectable({
  providedIn: 'root'
})
export class EmployeeIdUtilService {

  private static readonly PREFIX_APMOSYS_PRODUCT_CONSULTANT = 'APCS-';
  private static readonly PREFIX_APMOSYS_PRODUCT = 'AP-';
  private static readonly PREFIX_CONSULTANT = 'CS-';
  private static readonly PREFIX_REGULAR = 'A-';

  constructor() { }

  private isTrue(value: any): boolean {
    return value === true || value === 'true';
  }

  resolveCategory(isApmosysProduct: any, isConsultant: any): EmployeeIdCategory {
    if (this.isTrue(isApmosysProduct) && this.isTrue(isConsultant)) {
      return 'APCS';
    }
    if (this.isTrue(isApmosysProduct)) {
      return 'AP';
    }
    if (this.isTrue(isConsultant)) {
      return 'CS';
    }
    return 'A';
  }

  resolvePrefix(isApmosysProduct: any, isConsultant: any): string {
    const category = this.resolveCategory(isApmosysProduct, isConsultant);
    switch (category) {
      case 'APCS':
        return EmployeeIdUtilService.PREFIX_APMOSYS_PRODUCT_CONSULTANT;
      case 'AP':
        return EmployeeIdUtilService.PREFIX_APMOSYS_PRODUCT;
      case 'CS':
        return EmployeeIdUtilService.PREFIX_CONSULTANT;
      default:
        return EmployeeIdUtilService.PREFIX_REGULAR;
    }
  }

  resolveEmployeeType(isApmosysProduct: any, isConsultant: any, isApprenticeship?: any): string {
    if (this.isTrue(isApprenticeship)) {
      return 'Apprentice';
    }
    if (this.isTrue(isApmosysProduct) && this.isTrue(isConsultant)) {
      return 'Apmosys Product Consultant';
    }
    if (this.isTrue(isConsultant)) {
      return 'Consultant';
    }
    if (this.isTrue(isApmosysProduct)) {
      return 'Apmosys Product';
    }
    return 'Regular';
  }

  extractNumericId(prefixedId: any): string | null {
    if (!prefixedId || typeof prefixedId !== 'string') {
      return null;
    }
    const trimmed = prefixedId.trim();
    if (trimmed.startsWith(EmployeeIdUtilService.PREFIX_APMOSYS_PRODUCT_CONSULTANT)) {
      return trimmed.substring(EmployeeIdUtilService.PREFIX_APMOSYS_PRODUCT_CONSULTANT.length) || null;
    }
    if (trimmed.startsWith(EmployeeIdUtilService.PREFIX_APMOSYS_PRODUCT)) {
      return trimmed.substring(EmployeeIdUtilService.PREFIX_APMOSYS_PRODUCT.length) || null;
    }
    if (trimmed.startsWith(EmployeeIdUtilService.PREFIX_CONSULTANT)) {
      return trimmed.substring(EmployeeIdUtilService.PREFIX_CONSULTANT.length) || null;
    }
    if (trimmed.startsWith(EmployeeIdUtilService.PREFIX_REGULAR)) {
      return trimmed.substring(EmployeeIdUtilService.PREFIX_REGULAR.length) || null;
    }
    const parts = trimmed.split('-');
    return parts.length === 2 && parts[1] ? parts[1] : null;
  }

  generateEmploymentId(
    id: any,
    isApmosysProduct: any,
    isConsultant: any
  ): string | null {
    if (id == null || id === '') {
      return null;
    }
    const numericId = typeof id === 'string' && id.includes('-')
      ? this.extractNumericId(id) ?? id
      : String(id);
    return `${this.resolvePrefix(isApmosysProduct, isConsultant)}${numericId}`;
  }

}
