/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { ProjectInsightDomainServiceService } from './ProjectInsightDomainService.service';

describe('Service: ProjectInsightDomainService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ProjectInsightDomainServiceService]
    });
  });

  it('should ...', inject([ProjectInsightDomainServiceService], (service: ProjectInsightDomainServiceService) => {
    expect(service).toBeTruthy();
  }));
});
