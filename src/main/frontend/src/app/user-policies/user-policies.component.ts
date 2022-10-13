import { Component, OnInit } from '@angular/core';
import { User } from 'src/app/models/user';
import { PoliciesService } from '../services/policies.service';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { saveAs } from "file-saver";




@Component({
  selector: 'app-user-policies',
  templateUrl: './user-policies.component.html',
  styleUrls: ['./user-policies.component.css']
})
export class UserPoliciesComponent implements OnInit {
  currentUser: User;

  constructor(private policiesService : PoliciesService,
    private authenticationService: AuthenticationService,
    private modalService: BsModalService,

  ) { 
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);

  }
  document:any[] = [];

  ngOnInit(): void {

    this.getAllDocuments();
  }


  getAllDocuments(){
    this.document = [];
    this.policiesService.getAllDocument().pipe(first()).subscribe((response:any) => {
      if (response.serviceStatus == "Success") {
        this.document =  response.serviceResponse;
        console.log("DocumentList : ", this.document);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }
  
  downloadFile(doc: any) {
    this.policiesService.downloadDocument( doc.policyID).subscribe(blob => saveAs(blob,doc.fileName));
  }
  page = 1;
  handlePageChange(event) {
    this.page = event;
  }

}