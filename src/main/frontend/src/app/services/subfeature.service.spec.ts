import { TestBed } from '@angular/core/testing';

import { SubfeatureService } from './subfeature.service';

describe('SubfeatureService', () => {
  let service: SubfeatureService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SubfeatureService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
