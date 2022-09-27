import { Component, OnInit, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-employee-update-list',
  templateUrl: './employee-update-list.component.html',
  styleUrls: ['./employee-update-list.component.css']
})
export class EmployeeUpdateListComponent implements OnInit {
  myDraftList:any[] = [];
  @Output() draftEdit:EventEmitter<any> = new EventEmitter<any>();

  constructor() { }

  ngOnInit(): void {

  }

  onEdit(){
    this.draftEdit.emit(false);
  }

  onDelete(){

  }
    
}
