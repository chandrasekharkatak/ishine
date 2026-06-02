import { Injectable } from '@angular/core';

export type EmployeeIdCategory = 'A' | 'CS' | 'AP' | 'APCS';

@Injectable({
  providedIn: 'root'
})
export class EmployeeIdUtilService {

  static readonly PREFIX_APMOSYS_PRODUCT_CONSULTANT = 'APCS-';
  static readonly PREFIX_APMOSYS_PRODUCT = 'AP-';
  static readonly PREFIX_CONSULTANT = 'CS-';
  static readonly PREFIX_REGULAR = 'A-';
  static readonly EMPLOYMENT_ID_PREFIX_PATTERN = /^(A-|CS-|AP-|APCS-)/i;

  constructor() { }

  private isTrue(value: any): boolean {
    if (value === true || value === 1) {
      return true;
    }
    const normalized = String(value ?? '').trim().toLowerCase();
    return normalized === 'true' || normalized === '1' || normalized === 'y' || normalized === 'yes';
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

  /** UI-facing employee type label (ApMoSys branding for product types). */
  formatEmployeeTypeLabel(
    employeeTypeOrEmp: string | any,
    isConsultant?: any,
    isApmosysProduct?: any,
    isApprenticeship?: any
  ): string {
    let type: string;
    if (employeeTypeOrEmp != null && typeof employeeTypeOrEmp === 'object') {
      const emp = employeeTypeOrEmp;
      if (emp.employeeType && emp.employeeType !== 'On roll') {
        type = String(emp.employeeType);
      } else {
        type = this.resolveEmployeeType(emp.isApmosysProduct, emp.isConsultant, emp.isApprenticeship);
      }
    } else {
      type = String(employeeTypeOrEmp ?? '');
      if (!type || type === 'On roll') {
        type = this.resolveEmployeeType(isApmosysProduct, isConsultant, isApprenticeship);
      }
    }
    switch (type) {
      case 'Apmosys Product Consultant':
      case 'ApMoSys Product Consultant':
        return 'ApMoSys Product Consultant';
      case 'Apmosys Product':
      case 'ApMoSys Product':
        return 'ApMoSys Product';
      default:
        return type;
    }
  }

  formatEmploymentIdForDisplay(emp: any): string | null {
    if (!emp) {
      return null;
    }
    return this.generateEmploymentId(
      emp.employeementId ?? emp.employmentIdAcToET ?? emp.employmentId,
      emp.isApmosysProduct,
      emp.isConsultant,
      emp.employeeType
    );
  }

  private inferFlagsFromEmployeeType(employeeType: any): { isApmosysProduct: boolean; isConsultant: boolean } {
    const type = String(employeeType ?? '').trim();
    if (!type) {
      return { isApmosysProduct: false, isConsultant: false };
    }
    if (/apmosys\s*product\s*consultant/i.test(type)) {
      return { isApmosysProduct: true, isConsultant: true };
    }
    if (/apmosys\s*product/i.test(type) || type === 'ApmosysProduct') {
      return { isApmosysProduct: true, isConsultant: false };
    }
    if (/^consultant$/i.test(type)) {
      return { isApmosysProduct: false, isConsultant: true };
    }
    return { isApmosysProduct: false, isConsultant: false };
  }

  private needsReformatForEmployeeType(prefixedId: string, employeeType: any): boolean {
    const type = String(employeeType ?? '').trim();
    if (/apmosys\s*product\s*consultant/i.test(type)) {
      return !prefixedId.toUpperCase().startsWith(EmployeeIdUtilService.PREFIX_APMOSYS_PRODUCT_CONSULTANT);
    }
    return false;
  }

  applyEmployeeDisplayFields(emp: any): void {
    if (!emp) {
      return;
    }
    emp.employeeType = this.formatEmployeeTypeLabel(emp);
    const formattedId = this.formatEmploymentIdForDisplay(emp);
    if (formattedId) {
      emp.employmentIdAcToET = formattedId;
    }
  }

  /** Strip A-/CS-/AP-/APCS- prefix for API payloads (backend expects numeric Long). */
  toApiEmploymentId(id: any): number | string | null {
    if (id == null || id === '') {
      return id;
    }
    if (typeof id === 'number') {
      return id;
    }
    const str = String(id).trim();
    const extracted = this.extractNumericId(str);
    if (extracted != null) {
      const parsed = Number(extracted);
      return Number.isNaN(parsed) ? extracted : parsed;
    }
    if (!str.includes('-')) {
      const parsed = Number(str);
      return Number.isNaN(parsed) ? str : parsed;
    }
    return null;
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
    isConsultant: any,
    employeeType?: any
  ): string | null {
    if (id == null || id === '') {
      return null;
    }
    const str = String(id).trim();
    if (
      EmployeeIdUtilService.EMPLOYMENT_ID_PREFIX_PATTERN.test(str)
      && !this.needsReformatForEmployeeType(str, employeeType)
    ) {
      return str;
    }
    let ap = isApmosysProduct;
    let cs = isConsultant;
    if (!this.isTrue(ap) && !this.isTrue(cs) && employeeType) {
      const inferred = this.inferFlagsFromEmployeeType(employeeType);
      ap = inferred.isApmosysProduct;
      cs = inferred.isConsultant;
    }
    const numericId = str.includes('-')
      ? this.extractNumericId(str) ?? str
      : str;
    return `${this.resolvePrefix(ap, cs)}${numericId}`;
  }

}
