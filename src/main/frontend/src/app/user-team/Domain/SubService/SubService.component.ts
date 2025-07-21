import { Component, Input } from '@angular/core';
import { SubService } from '../Type';

@Component({
  selector: 'app-SubService',
  templateUrl: './SubService.component.html',
  styleUrls: ['./SubService.component.scss']
})
export class SubServiceComponent {

  @Input() subServiceList: SubService[] = []
  constructor() { }

  addChildrenSubServiceList(ind: number) {
    this.subServiceList[ind].subServiceChildren.push({isOpen: true, subService: '', subServiceChildren: []})
  }

  removeChildrenSubServiceList(ind: number) {
    this.subServiceList.splice(ind, 1)
  }

  toggleSubService(ind: number) {
    this.subServiceList[ind].isOpen = !this.subServiceList[ind].isOpen
  }

}
