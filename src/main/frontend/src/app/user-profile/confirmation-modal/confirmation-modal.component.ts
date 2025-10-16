import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { DepartmentService } from 'src/app/services/department.service';

@Component({
  selector: 'app-confirmation-modal',
  templateUrl: './confirmation-modal.component.html',
  styleUrls: ['./confirmation-modal.component.css']
})
export class ConfirmationModalComponent implements OnInit {

  constructor( private departmentService: DepartmentService) { }

  ngOnInit(): void {
  }

  @Input() title: string = 'Confirm Action';
  @Input() message: string = 'Are you sure?';
  @Input() confirmText: string = 'Yes';
  @Input() cancelText: string = 'Cancel';
  @Input() data: any; 

  @Output() confirm = new EventEmitter<any>();
  @Output() cancel = new EventEmitter<void>();

  onConfirm() {
    this.confirm.emit(this.data);  
  }

  onCancel() {
    this.cancel.emit();
  }

}
