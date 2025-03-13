import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationService } from '../services/authentication.service';
import { User } from '../models/user';

@Component({
  selector: 'app-qr-code-generator',
  templateUrl: './qr-code-generator.component.html',
  styleUrls: ['./qr-code-generator.component.css']
})
export class QrCodeGeneratorComponent implements OnInit {

  currentUser: User;

  constructor(
    private authenticationService: AuthenticationService,
    private router: Router,
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
   }

  ngOnInit(): void {

    this.redirectToQrCodeGenerator();
  }

  redirectToQrCodeGenerator() {
    let currentUserSession = new User();
    currentUserSession.email = this.currentUser.email;
    currentUserSession.password = this.currentUser.email+'123';
    currentUserSession.firstName = this.currentUser.name;

    const encodedSession = btoa(JSON.stringify(currentUserSession));
    
    window.open(`http://localhost:4201/#/dashboard?session=${encodedSession}`, '_blank');
    this.router.navigate(['/home']);
    // window.open(`https://moccrcsstaging.crcs.gov.in/saharasupport/#/supportAdmin/application-info?session=${encodedSession}`, '_blank');
  }

}
