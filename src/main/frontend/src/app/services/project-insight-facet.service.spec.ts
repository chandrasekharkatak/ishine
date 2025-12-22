import { TestBed } from '@angular/core/testing';

import { ProjectInsightFacetService } from './project-insight-facet.service';

describe('ProjectInsightFacetService', () => {
  let service: ProjectInsightFacetService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ProjectInsightFacetService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
