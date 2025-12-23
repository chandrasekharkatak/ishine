/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { SubDomainComponent } from './SubDomain.component';

describe('SubDomainComponent', () => {
  let component: SubDomainComponent;
  let fixture: ComponentFixture<SubDomainComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SubDomainComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SubDomainComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
