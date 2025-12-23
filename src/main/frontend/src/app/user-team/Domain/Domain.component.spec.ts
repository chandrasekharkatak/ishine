/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { DomainComponent } from './Domain.component';

describe('DomainComponent', () => {
  let component: DomainComponent;
  let fixture: ComponentFixture<DomainComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DomainComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DomainComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
