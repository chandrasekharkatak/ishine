import { Component, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-confirm-leave-ticket-modal',
  standalone: false,
  template: `
    <div class="modal-body text-center">
      <p class="mb-3">{{ message }}</p>
      <button type="button" class="btn btn-primary" (click)="ok()">OK</button>&nbsp;
      <button type="button" class="btn btn-secondary" (click)="cancel()">Cancel</button>
    </div>
  `
})
export class ConfirmLeaveTicketModalComponent {
  @Input() message =
    'Are you sure you want to leave this page?\n\n' +
    'You have unsaved reimbursement details that have not yet been submitted.\n' +
    'If you leave this page now, all entered information will be lost and you will need to re-enter the details later.';

  constructor(public activeModal: NgbActiveModal) {}

  ok(): void {
    this.activeModal.close(true);
  }

  cancel(): void {
    this.activeModal.dismiss(false);
  }
}

