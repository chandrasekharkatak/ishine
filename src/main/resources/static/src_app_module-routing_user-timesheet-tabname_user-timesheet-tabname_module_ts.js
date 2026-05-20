"use strict";
(self["webpackChunkemployee_portal_revamp"] = self["webpackChunkemployee_portal_revamp"] || []).push([["src_app_module-routing_user-timesheet-tabname_user-timesheet-tabname_module_ts"],{

/***/ 17928:
/*!************************************************************************************************!*\
  !*** ./src/app/module-routing/user-timesheet-tabname/user-timesheet-tabname-routing.module.ts ***!
  \************************************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   UserTimesheetTabnameRoutingModule: () => (/* binding */ UserTimesheetTabnameRoutingModule)
/* harmony export */ });
/* harmony import */ var _angular_router__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! @angular/router */ 18431);
/* harmony import */ var src_app_user_timesheet_biomax_approval_biomax_approval_component__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! src/app/user-timesheet/biomax-approval/biomax-approval.component */ 35727);
/* harmony import */ var src_app_user_timesheet_hr_dashboard_hr_dashboard_component__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! src/app/user-timesheet/hr-dashboard/hr-dashboard.component */ 2359);
/* harmony import */ var src_app_user_timesheet_my_timesheet_my_timesheet_component__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! src/app/user-timesheet/my-timesheet/my-timesheet.component */ 10919);
/* harmony import */ var src_app_user_timesheet_team_timesheet_team_timesheet_component__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! src/app/user-timesheet/team-timesheet/team-timesheet.component */ 14965);
/* harmony import */ var src_app_user_timesheet_user_timesheet_component__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! src/app/user-timesheet/user-timesheet.component */ 73206);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! @angular/core */ 37580);








const routes = [{
  path: '',
  component: src_app_user_timesheet_user_timesheet_component__WEBPACK_IMPORTED_MODULE_4__.UserTimesheetComponent,
  children: [{
    path: 'my-timesheet',
    component: src_app_user_timesheet_my_timesheet_my_timesheet_component__WEBPACK_IMPORTED_MODULE_2__.MyTimesheetComponent
  }, {
    path: 'team-timesheet',
    component: src_app_user_timesheet_team_timesheet_team_timesheet_component__WEBPACK_IMPORTED_MODULE_3__.TeamTimesheetComponent
  }, {
    path: 'biomax-request',
    component: src_app_user_timesheet_biomax_approval_biomax_approval_component__WEBPACK_IMPORTED_MODULE_0__.BiomaxApprovalComponent
  }, {
    path: 'hr-dashboard',
    component: src_app_user_timesheet_hr_dashboard_hr_dashboard_component__WEBPACK_IMPORTED_MODULE_1__.HrDashboardComponent
  }]
}];
class UserTimesheetTabnameRoutingModule {
  static {
    this.ɵfac = function UserTimesheetTabnameRoutingModule_Factory(__ngFactoryType__) {
      return new (__ngFactoryType__ || UserTimesheetTabnameRoutingModule)();
    };
  }
  static {
    this.ɵmod = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵdefineNgModule"]({
      type: UserTimesheetTabnameRoutingModule
    });
  }
  static {
    this.ɵinj = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵdefineInjector"]({
      imports: [_angular_router__WEBPACK_IMPORTED_MODULE_6__.RouterModule.forChild(routes), _angular_router__WEBPACK_IMPORTED_MODULE_6__.RouterModule]
    });
  }
}
(function () {
  (typeof ngJitMode === "undefined" || ngJitMode) && _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵsetNgModuleScope"](UserTimesheetTabnameRoutingModule, {
    imports: [_angular_router__WEBPACK_IMPORTED_MODULE_6__.RouterModule],
    exports: [_angular_router__WEBPACK_IMPORTED_MODULE_6__.RouterModule]
  });
})();

/***/ }),

/***/ 95897:
/*!****************************************************************************************!*\
  !*** ./src/app/module-routing/user-timesheet-tabname/user-timesheet-tabname.module.ts ***!
  \****************************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   UserTimesheetTabnameModule: () => (/* binding */ UserTimesheetTabnameModule)
/* harmony export */ });
/* harmony import */ var _angular_common__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/common */ 84460);
/* harmony import */ var _user_timesheet_tabname_routing_module__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./user-timesheet-tabname-routing.module */ 17928);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ 37580);



class UserTimesheetTabnameModule {
  static {
    this.ɵfac = function UserTimesheetTabnameModule_Factory(__ngFactoryType__) {
      return new (__ngFactoryType__ || UserTimesheetTabnameModule)();
    };
  }
  static {
    this.ɵmod = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵdefineNgModule"]({
      type: UserTimesheetTabnameModule
    });
  }
  static {
    this.ɵinj = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵdefineInjector"]({
      imports: [_angular_common__WEBPACK_IMPORTED_MODULE_2__.CommonModule, _user_timesheet_tabname_routing_module__WEBPACK_IMPORTED_MODULE_0__.UserTimesheetTabnameRoutingModule]
    });
  }
}
(function () {
  (typeof ngJitMode === "undefined" || ngJitMode) && _angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵsetNgModuleScope"](UserTimesheetTabnameModule, {
    imports: [_angular_common__WEBPACK_IMPORTED_MODULE_2__.CommonModule, _user_timesheet_tabname_routing_module__WEBPACK_IMPORTED_MODULE_0__.UserTimesheetTabnameRoutingModule]
  });
})();

/***/ })

}]);
//# sourceMappingURL=src_app_module-routing_user-timesheet-tabname_user-timesheet-tabname_module_ts.js.map