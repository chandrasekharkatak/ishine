import { Injectable } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ExcelDownloadHelperComponent } from '../helpers/excel-download-helper/excel-download-helper.component';

@Injectable({
  providedIn: 'root'
})
export class ExcelDownloadService {

  constructor(private modalService: NgbModal) {}

  openConfirmAndDownload(blob: Blob, fileName: string): void {
    const modalRef = this.modalService.open(ExcelDownloadHelperComponent, {
      centered: true
    });

    modalRef.componentInstance.downloadConfirmed.subscribe(() => {
      this.downloadBlob(blob, fileName);
    });
  }

  private downloadBlob(blob: Blob, fileName: string): void {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = fileName || 'document';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  }
}