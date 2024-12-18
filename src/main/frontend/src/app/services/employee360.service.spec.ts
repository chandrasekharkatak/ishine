import { TestBed } from '@angular/core/testing';

import { Employee360Service } from './employee360.service';

describe('Employee360Service', () => {
  let service: Employee360Service;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Employee360Service);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
