export interface KpiTemplate {
  kpiId?: number;
  name: string;
  description: string;
  createdBy: number;
  quarterId: number;
  kpis: {
    id?: number;
    description: string;
  }[];
}
