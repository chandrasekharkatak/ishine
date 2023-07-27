import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NewsletterConfigComponent } from './newsletter-config.component';

describe('NewsletterConfigComponent', () => {
  let component: NewsletterConfigComponent;
  let fixture: ComponentFixture<NewsletterConfigComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NewsletterConfigComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NewsletterConfigComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
