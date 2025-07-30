import { Component, Input } from '@angular/core';
import { Domain, SubService, Service, SubDomain } from '../Type';
import { ProjectInsightDomainServiceService } from 'src/app/services/ProjectInsightDomainService.service';

@Component({
  selector: 'app-SubService',
  templateUrl: './SubService.component.html',
  styleUrls: ['./SubService.component.scss']
})
export class SubServiceComponent {

  @Input() isViewing = false
  @Input() subServiceList: SubService[] = []
  @Input() isEditing = false

  constructor(private readonly projectInsightDomainService: ProjectInsightDomainServiceService) { }

  addChildrenSubServiceList(ind: number) {
    this.subServiceList[ind].subServices.push({isOpen: true,isActive: true, name: '', subServices: [], type: 'subService'})
  }

  newData(data:any){
    return !data.id;
  }

  removeChildrenSubServiceList(ind: number) {
    this.subServiceList.splice(ind, 1)
  }

  toggleSubService(ind: number) {
    this.subServiceList[ind].isOpen = !this.subServiceList[ind].isOpen
  }

  deleteDomainData(id: number, type: string, domain: Domain | Service| SubDomain | SubService, isActive: boolean) {
      this.projectInsightDomainService.deleteDomainData(id, type).subscribe({
        next: (res: any) => {
          console.log("Deleted Domain: ", res);
          domain.isActive = !isActive
        },
        error: (error: any) => {
          console.error("Error: ", error);
        }
      })
    }

}
