export interface Kpi {
  id?: number;
  name: string;
  description: string;
  createdBy: number;
  approvedBy?: string;   
  createdAt?: Date;
  updatedAt?: Date;
  department?: string;      
  quarterId?: number;
  kpis: KpiItem[];
}

export interface KpiItem {
  id?: number;
  kpiText: string;     
}
