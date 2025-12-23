import { TestBed } from '@angular/core/testing';

import { TeamDashboardService } from './team-dashboard.service';

describe('TeamDashboardService', () => {
  let service: TeamDashboardService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TeamDashboardService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
