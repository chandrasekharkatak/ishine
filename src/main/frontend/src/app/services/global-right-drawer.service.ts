import { Injectable, TemplateRef } from '@angular/core';
import { BodyComponent } from '../body/body.component';

@Injectable({
  providedIn: 'root'
})

export class GlobalRightDrawerService {

  private bodyComponent!: BodyComponent;

  register(body: BodyComponent) {
    this.bodyComponent = body;
  }

  open(template: TemplateRef<any>) {
    this.bodyComponent.openDrawer(template);
  }

  close() {
    this.bodyComponent.closeDrawer();
  }
}
