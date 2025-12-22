import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectInsightQuestionLibraryComponent } from './project-insight-question-library.component';

describe('ProjectInsightQuestionLibraryComponent', () => {
  let component: ProjectInsightQuestionLibraryComponent;
  let fixture: ComponentFixture<ProjectInsightQuestionLibraryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ProjectInsightQuestionLibraryComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectInsightQuestionLibraryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
