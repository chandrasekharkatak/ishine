import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

export interface ModalEvent {
  action: 'OPEN' | 'CLOSE' | 'CLOSE_ALL';
  id?: string;
  modalType?: string;
  data?: any;
}

@Injectable({
  providedIn: 'root'
})

export class AppModalService {

  private rmgModalSubject = new Subject<ModalEvent>();
  rmgModal$ = this.rmgModalSubject.asObservable();

  private rmgActionSubject = new Subject<any>();
  rmgAction$ = this.rmgActionSubject.asObservable();

  open(id: string, modalType: string, data?: any) {
    this.rmgModalSubject.next({ action: 'OPEN', id, modalType, data });
  }

  close(id: string) {
    this.rmgModalSubject.next({ action: 'CLOSE', id });
  }

  closeAll() {
    this.rmgModalSubject.next({ action: 'CLOSE_ALL' });
  }

  triggerAction(action: any) {
    this.rmgActionSubject.next(action);
  }

}
