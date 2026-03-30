import { TestBed } from '@angular/core/testing';

import { RmgModalService } from './app-modal.service';

describe('RmgModalService', () => {
  let service: RmgModalService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(RmgModalService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
