import { TestBed } from '@angular/core/testing';

import { GlobalRightDrawerService } from './global-right-drawer.service';

describe('GlobalRightDrawerService', () => {
  let service: GlobalRightDrawerService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(GlobalRightDrawerService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
