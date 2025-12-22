import { TestBed } from '@angular/core/testing';

import { ProjectInsightDomainService } from './project-insight-domain.service';

describe('ProjectInsightDomainService', () => {
  let service: ProjectInsightDomainService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ProjectInsightDomainService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
