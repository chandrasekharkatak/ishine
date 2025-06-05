// This file can be replaced during build by using the `fileReplacements` array.
// `ng build` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

export const environment = {
  production: false,
  // baseUrl: "https://ishine.apmosys.com/",
    baseUrl: "http://localhost:8080/",
  //baseUrl: "http://192.168.21.175:8081/employeeportal/",

  //  baseUrl360: "http://localhost:4200/",
  baseUrl360: "https://ishine.apmosys.com/",
  
  lmsbaseurl :"http://192.168.12.113/academy-lms/index.php/",
  //lmsbaseurl :"http://localhost/academy-lms/index.php/",
  qrCodebaseUrl: "http://192.168.12.108:8080/qrcodegenerator/#/",

};
// 1. Uncomment all the logout user functionality employee interceptor line 41 to 53 and authentication service line 93
// 2. for backend loggedInUserAudit() line userSessionRepository.deleteById(session.getUserSessionId()); and idle.session.timeout= 86400 to 900
/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
// import 'zone.js/plugins/zone-error';  // Included with Angular CLI.
   
