import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { UpdateUserInfoService } from 'src/app/services/updateUserInfo.service';

@Component({
  selector: 'app-document-upload',
  templateUrl: './document-upload.component.html',
  styleUrls: ['./document-upload.component.css']
})
export class DocumentUploadComponent implements OnInit {

  constructor(
    private updateUserInfoService: UpdateUserInfoService,
    private router: Router,
    private route:ActivatedRoute,
  ) { }

  ngOnInit(): void {
  }

  onSave(){
    this.router.navigate(['../info-preview'], {relativeTo:this.route});
  }

}
