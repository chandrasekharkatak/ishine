import { TestBed } from '@angular/core/testing';

import { ApiSourceService } from './api-source.service';

describe('ApiSourceService', () => {
  let service: ApiSourceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ApiSourceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
