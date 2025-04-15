import { TestBed } from '@angular/core/testing';

import { BiomaxseviceService } from './biomaxsevice.service';

describe('BiomaxseviceService', () => {
  let service: BiomaxseviceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(BiomaxseviceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
