import { TestBed } from '@angular/core/testing';

import { TravelDeskService } from './travel-desk.service';

describe('TravelDeskService', () => {
  let service: TravelDeskService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TravelDeskService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
