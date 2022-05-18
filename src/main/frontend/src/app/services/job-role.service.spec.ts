import { TestBed } from '@angular/core/testing';

import { JobRoleService } from './job-role.service';

describe('JobRoleService', () => {
  let service: JobRoleService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(JobRoleService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
