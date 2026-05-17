import { Component, Output,EventEmitter } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
// import { EventEmitter } from 'stream';

@Component({
  selector: 'app-excel-download-helper',
  imports: [],
  templateUrl: './excel-download-helper.component.html',
  styleUrl: './excel-download-helper.component.css'
})
export class ExcelDownloadHelperComponent {

  @Output() downloadConfirmed = new EventEmitter();

  constructor(public activeModal: NgbActiveModal) {}


  onDownload(): void {
    this.downloadConfirmed.emit(null);
    this.activeModal.close('download');
  }
}
