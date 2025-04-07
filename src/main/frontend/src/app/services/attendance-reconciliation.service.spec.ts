import { TestBed } from '@angular/core/testing';

import { AttendanceReconciliationService } from './attendance-reconciliation.service';

describe('AttendanceReconciliationService', () => {
  let service: AttendanceReconciliationService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AttendanceReconciliationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
