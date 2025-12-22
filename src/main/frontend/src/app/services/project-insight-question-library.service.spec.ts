import { TestBed } from '@angular/core/testing';

import { ProjectInsightQuestionLibraryService } from './project-insight-question-library.service';

describe('ProjectInsightQuestionLibraryService', () => {
  let service: ProjectInsightQuestionLibraryService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ProjectInsightQuestionLibraryService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
