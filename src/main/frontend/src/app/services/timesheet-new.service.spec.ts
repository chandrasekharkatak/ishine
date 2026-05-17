import { TestBed } from '@angular/core/testing';

import { TimesheetNewService } from './timesheet-new.service';

describe('TimesheetNewService', () => {
  let service: TimesheetNewService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TimesheetNewService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
