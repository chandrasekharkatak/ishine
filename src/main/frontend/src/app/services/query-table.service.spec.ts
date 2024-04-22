import { TestBed } from '@angular/core/testing';

import { QueryTableService } from './query-table.service';

describe('QueryTableService', () => {
  let service: QueryTableService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(QueryTableService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
