import { TestBed } from '@angular/core/testing';

import { UserPerformanceService } from './user-performance.service';

describe('UserPerformanceService', () => {
  let service: UserPerformanceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(UserPerformanceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
