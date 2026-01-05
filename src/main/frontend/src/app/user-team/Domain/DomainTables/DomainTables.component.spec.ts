/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { DomainTablesComponent } from './DomainTables.component';

describe('DomainTablesComponent', () => {
  let component: DomainTablesComponent;
  let fixture: ComponentFixture<DomainTablesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DomainTablesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DomainTablesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
