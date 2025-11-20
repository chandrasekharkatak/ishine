import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';

@Injectable({
  providedIn: 'root'
})
export class ExportExcelService {
  excelName: any;
  tableName: any;

  constructor() { }


  exportTableDataToExcel(arr: any[], name: string){
    const worksheet: XLSX.WorkSheet = XLSX.utils.json_to_sheet(arr);

      const book: XLSX.WorkBook = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(book, worksheet, 'Sheet1');

      XLSX.writeFile(book, name);
  }

  exportTableDataToExcelWithDescription(arr: any[], name: string) {
    const worksheet: XLSX.WorkSheet = XLSX.utils.aoa_to_sheet(arr);

    const book: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(book, worksheet, 'Sheet1');

    XLSX.writeFile(book, name);
  }
  exportTableFormat(tableId,excelName,tableName)
  {

        this.excelName = excelName;
        this.tableName = tableName;
        const table = document.getElementById(tableId);
        if (!table) {
          console.error('Table not found');
          return;
        }
        
      
        const worksheet: XLSX.WorkSheet = XLSX.utils.table_to_sheet(table);
        const workbook: XLSX.WorkBook = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(workbook, worksheet,tableName);
      
        const excelBuffer: any = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
        const data: Blob = new Blob([excelBuffer], { type: 'application/octet-stream' });
      
        saveAs(data, this.excelName);
  }
  
  exportGenericTableDataToExcel(arr: any[][], name: string) {
    const worksheet: XLSX.WorkSheet = XLSX.utils.aoa_to_sheet(arr);
    const workbook: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Sheet1');
    XLSX.writeFile(workbook, name);
  }

}
