import { TestBed } from '@angular/core/testing';

import { ProjectInsightProjconfigService } from './project-insight-projconfig.service';

describe('ProjectInsightProjconfigService', () => {
  let service: ProjectInsightProjconfigService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ProjectInsightProjconfigService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
