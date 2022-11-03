import { Component, OnInit } from '@angular/core';
import { first } from 'rxjs/operators';
import { PortalService } from 'src/app/services/portal.service';
import { DomSanitizer } from '@angular/platform-browser';

@Component({
  selector: 'app-recruitment',
  templateUrl: './recruitment.component.html',
  styleUrls: ['./recruitment.component.css']
})
export class RecruitmentComponent implements OnInit {

  portalConfigList:any[] = [];
  recruitmentURL:any;

  constructor(
    private portalService:PortalService,
    private sanitizer: DomSanitizer
  ) { }

  ngOnInit(): void {
    this.getAllPortalConfigData();
  }

  getAllPortalConfigData() {
    this.portalService.getPortalConfig().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.portalConfigList = response.serviceResponse;

        console.log(this.portalConfigList,  "   :  portalConfigList");

        for(let portal of this.portalConfigList){
          if(portal.configName == 'OTRS Link'){
            this.recruitmentURL = this.sanitizer.bypassSecurityTrustUrl(portal.configValue);
            this.recruitmentURL = this.recruitmentURL.changingThisBreaksApplicationSecurity;
            console.log(this.recruitmentURL, " this.recruitmentURL");

            let otrsIframe = document.getElementById('recruitment-iframe');
            otrsIframe.setAttribute('src', this.recruitmentURL);
          }
        }
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

}
