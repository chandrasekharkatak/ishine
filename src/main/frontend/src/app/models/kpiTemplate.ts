export interface KpiTemplate {
  id?: number;
  name: string;
  description: string;
  createdBy: number;
  quarterId: number;
  departmentId: number;
  kpis: {
    id?: number;
    description: string;
  }[];
  employee_role: string;
}
