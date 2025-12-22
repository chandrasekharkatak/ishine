import { Component, OnInit, Output, EventEmitter, SecurityContext, Input, TemplateRef } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { Employee } from 'src/app/models/employee';
import { ImageService } from 'src/app/services/image.service';

@Component({
  standalone: false,
  selector: 'app-employee-update-list',
  templateUrl: './employee-update-list.component.html',
  styleUrls: ['./employee-update-list.component.css']
})
export class EmployeeUpdateListComponent implements OnInit {
  myDraftList:any[] = [];
  @Output() draftEdit:EventEmitter<any> = new EventEmitter<any>();
  @Output() showPreview:EventEmitter<any> = new EventEmitter<any>();
  @Output() draftRevoke:EventEmitter<any> = new EventEmitter<any>();
  @Output() draftDelete:EventEmitter<any> = new EventEmitter<any>();



  constructor() {
    
   }

  ngOnInit(): void {

  }

  onEdit(){
    this.draftEdit.emit(false);
  }

  onDelete(){
    this.draftDelete.emit();
  }

  onRevoke(){
    this.draftRevoke.emit();
  }

  onShowPreview(){
    this.showPreview.emit();
  }
    
}
