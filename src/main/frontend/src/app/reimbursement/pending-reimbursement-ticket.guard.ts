import { Injectable } from '@angular/core';
import { CanDeactivate } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { MyReimbursementComponent } from './my-reimbursement/my-reimbursement.component';
import { ConfirmLeaveTicketModalComponent } from './confirm-leave-ticket-modal.component';

@Injectable({ providedIn: 'root' })
export class PendingReimbursementTicketGuard implements CanDeactivate<MyReimbursementComponent> {
  constructor(private modalService: NgbModal) {}

  canDeactivate(component: MyReimbursementComponent): boolean | Promise<boolean> {
    const hasPending =
      typeof (component as any).hasPendingTicket === 'function'
        ? (component as any).hasPendingTicket()
        : Array.isArray((component as any).ticketClaims) && (component as any).ticketClaims.length > 0;

    if (!hasPending) {
      return true;
    }

    const ref = this.modalService.open(ConfirmLeaveTicketModalComponent, { modalDialogClass: 'modal-sm' });
    ref.componentInstance.message =
      'Are you sure you want to leave this page?\n\n' +
      'You have unsaved reimbursement details that have not yet been submitted.\n' +
      'If you leave this page now, all entered information will be lost and you will need to re-enter the details later.';
    return ref.result
      .then(() => true)
      .catch(() => false);
  }
}

