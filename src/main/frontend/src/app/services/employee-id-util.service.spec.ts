import { TestBed } from '@angular/core/testing';

import { EmployeeIdUtilService } from './employee-id-util.service';

describe('EmployeeIdUtilService', () => {
  let service: EmployeeIdUtilService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(EmployeeIdUtilService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
