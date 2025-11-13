/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { FilterProjectInsightComponent } from './filter-project-insight.component';

describe('FilterProjectInsightComponent', () => {
  let component: FilterProjectInsightComponent;
  let fixture: ComponentFixture<FilterProjectInsightComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ FilterProjectInsightComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FilterProjectInsightComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
