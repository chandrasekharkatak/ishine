import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GroupBrowserComponent } from './group-browser.component';

describe('GroupBrowserComponent', () => {
  let component: GroupBrowserComponent;
  let fixture: ComponentFixture<GroupBrowserComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GroupBrowserComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GroupBrowserComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
