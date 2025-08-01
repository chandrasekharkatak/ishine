import { TestBed } from '@angular/core/testing';

import { TeamEmployeeTimesheetService } from './team-employee-timesheet.service';

describe('TeamEmployeeTimesheetService', () => {
  let service: TeamEmployeeTimesheetService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TeamEmployeeTimesheetService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
