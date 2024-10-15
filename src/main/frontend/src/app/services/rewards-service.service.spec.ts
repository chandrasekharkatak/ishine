import { TestBed } from '@angular/core/testing';

import { RewardsServiceService } from './rewards-service.service';

describe('RewardsServiceService', () => {
  let service: RewardsServiceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(RewardsServiceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
