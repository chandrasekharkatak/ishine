import { TestBed } from '@angular/core/testing';

import { AppreciationService } from './appreciation.service';

describe('AppreciationService', () => {
  let service: AppreciationService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AppreciationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
