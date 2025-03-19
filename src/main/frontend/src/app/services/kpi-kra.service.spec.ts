import { TestBed } from '@angular/core/testing';

import { KpiKraService } from './kpi-kra.service';

describe('KpiKraService', () => {
  let service: KpiKraService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(KpiKraService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
