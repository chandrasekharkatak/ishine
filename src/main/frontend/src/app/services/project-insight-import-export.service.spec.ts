import { TestBed } from '@angular/core/testing';

import { ProjectInsightImportExportService } from './project-insight-import-export.service';

describe('ProjectInsightImportExportService', () => {
  let service: ProjectInsightImportExportService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ProjectInsightImportExportService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
