import { TestBed } from '@angular/core/testing';

import { ProjectInsightService } from './project-insight.service';

describe('ProjectInsightService', () => {
  let service: ProjectInsightService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ProjectInsightService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
