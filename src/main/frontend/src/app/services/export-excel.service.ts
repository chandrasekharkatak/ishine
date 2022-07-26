import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';

@Injectable({
  providedIn: 'root'
})
export class ExportExcelService {

  constructor() { }


  exportTableDataToExcel(arr: any[], name: string){
    const worksheet: XLSX.WorkSheet = XLSX.utils.json_to_sheet(arr);

      const book: XLSX.WorkBook = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(book, worksheet, 'Sheet1');

      XLSX.writeFile(book, name);
  }

}
