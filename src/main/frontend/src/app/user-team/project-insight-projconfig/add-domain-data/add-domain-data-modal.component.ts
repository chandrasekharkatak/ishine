// AddDataModal.component.ts
import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-domain-add-data-modal',
  templateUrl: './add-domain-data-modal.component.html',
  styleUrls: ['./add-domain-data-modal.component.scss']
})
export class AddDomainDataModalComponent {

  @Input() isVisible: boolean = false;
  @Input() title: string = 'Add New Item';
  @Input() placeholder: string = 'Enter name';
  @Output() onClose = new EventEmitter<void>();
  @Output() onSave = new EventEmitter<string>();
  @Output() changeChildType = new EventEmitter<string>();
  @Input() parentType: string = '';

  itemName: string = '';

  close() {
    this.itemName = '';
    this.onClose.emit();
  }

  save() {
    if (this.itemName.trim()) {
      this.onSave.emit(this.itemName.trim());
      this.itemName = '';
    }
  }

  changeChild(event: Event) {
    const childType = (event.target as HTMLInputElement).value;
    if (!childType || childType.trim() === '') return;
    this.changeChildType.emit(childType);
  }
}