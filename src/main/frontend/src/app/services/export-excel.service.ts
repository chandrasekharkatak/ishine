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

  exportDynamicMultiExcelSheetWithDynamicHeaders(sheets:{sheetName:string,headers:string[],data:any[]}[],fileName:string){

    const workbook: XLSX.WorkBook = XLSX.utils.book_new();
    sheets.forEach((sheet)=>{
      const aoa :any[][]=[];
      aoa.push(sheet.headers);
      sheet.data.forEach((row)=>{
        const rowData = sheet.headers.map((header) => row[header]);
        aoa.push(rowData);
      })
      const ws: XLSX.WorkSheet = XLSX.utils.aoa_to_sheet(aoa);
      
      const colWidths = sheet.headers.map(h => ({ wch: h.length + 10 }));
      ws['!cols'] = colWidths;
      
      this.applyHeaderStyles(ws,sheet.headers);
      // Append sheet
      XLSX.utils.book_append_sheet(workbook, ws, sheet.sheetName);



    })
    XLSX.writeFile(workbook, fileName);
  }

  applyHeaderStyles(worksheet: XLSX.WorkSheet, headers: string[]) {
    // Loop through each header column (same as your working code)
    headers.forEach((header, colIndex) => {
      const cellAddress = XLSX.utils.encode_cell({ c: colIndex, r: 0 });
  
      // Apply styles exactly like your working code
      if (worksheet[cellAddress]) {
        worksheet[cellAddress].s = {
          font: { 
            bold: true, 
            color: { rgb: "FFFFFF" } 
          },
          fill: { 
            fgColor: { rgb: "193D8A" ,patternType: "solid"}  // Dark blue
          },
          alignment: { 
            horizontal: "center", 
            vertical: "center", 
            wrapText: true 
          },
          border: {
            top: { style: "thin", color: { rgb: "000000" } },
            bottom: { style: "thin", color: { rgb: "000000" } },
            left: { style: "thin", color: { rgb: "000000" } },
            right: { style: "thin", color: { rgb: "000000" } }
          }
        };
      }
    });
  }

}
