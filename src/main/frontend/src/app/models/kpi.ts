export class Kpi {
    id?: number;
    kpiDescription?: string;
  
    constructor(
      id?: number,
      kpiDescription: string = ''
    ) {
      this.id = id;
      this.kpiDescription = kpiDescription;
    }
  }