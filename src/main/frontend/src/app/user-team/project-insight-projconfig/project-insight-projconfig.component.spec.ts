import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectInsightProjconfigComponent } from './project-insight-projconfig.component';

describe('ProjectInsightProjconfigComponent', () => {
  let component: ProjectInsightProjconfigComponent;
  let fixture: ComponentFixture<ProjectInsightProjconfigComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ProjectInsightProjconfigComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectInsightProjconfigComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
