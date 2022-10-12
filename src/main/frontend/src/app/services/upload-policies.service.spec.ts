import { TestBed } from '@angular/core/testing';

import { UploadPoliciesService } from './upload-policies.service';

describe('UploadPoliciesService', () => {
  let service: UploadPoliciesService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(UploadPoliciesService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
