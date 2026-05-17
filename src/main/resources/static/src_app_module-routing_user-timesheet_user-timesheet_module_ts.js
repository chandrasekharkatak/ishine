"use strict";
(self["webpackChunkemployee_portal_revamp"] = self["webpackChunkemployee_portal_revamp"] || []).push([["src_app_module-routing_user-timesheet_user-timesheet_module_ts"],{

/***/ 2467:
/*!************************************************************************!*\
  !*** ./src/app/module-routing/user-timesheet/user-timesheet.module.ts ***!
  \************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   UserTimesheetModule: () => (/* binding */ UserTimesheetModule)
/* harmony export */ });
/* harmony import */ var _angular_common__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/common */ 84460);
/* harmony import */ var _user_timesheet_routing_module__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./user-timesheet-routing.module */ 55258);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ 37580);



class UserTimesheetModule {
  static {
    this.ɵfac = function UserTimesheetModule_Factory(__ngFactoryType__) {
      return new (__ngFactoryType__ || UserTimesheetModule)();
    };
  }
  static {
    this.ɵmod = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵdefineNgModule"]({
      type: UserTimesheetModule
    });
  }
  static {
    this.ɵinj = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵdefineInjector"]({
      imports: [_angular_common__WEBPACK_IMPORTED_MODULE_2__.CommonModule, _user_timesheet_routing_module__WEBPACK_IMPORTED_MODULE_0__.UserTimesheetRoutingModule]
    });
  }
}
(function () {
  (typeof ngJitMode === "undefined" || ngJitMode) && _angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵsetNgModuleScope"](UserTimesheetModule, {
    imports: [_angular_common__WEBPACK_IMPORTED_MODULE_2__.CommonModule, _user_timesheet_routing_module__WEBPACK_IMPORTED_MODULE_0__.UserTimesheetRoutingModule]
  });
})();

/***/ }),

/***/ 55258:
/*!********************************************************************************!*\
  !*** ./src/app/module-routing/user-timesheet/user-timesheet-routing.module.ts ***!
  \********************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   UserTimesheetRoutingModule: () => (/* binding */ UserTimesheetRoutingModule)
/* harmony export */ });
/* harmony import */ var _angular_router__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! @angular/router */ 18431);
/* harmony import */ var src_app_user_timesheet_biomax_approval_biomax_approval_component__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! src/app/user-timesheet/biomax-approval/biomax-approval.component */ 35727);
/* harmony import */ var src_app_user_timesheet_calendar_view_calendar_view_component__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! src/app/user-timesheet/calendar-view/calendar-view.component */ 94155);
/* harmony import */ var src_app_user_timesheet_hr_dashboard_hr_dashboard_component__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! src/app/user-timesheet/hr-dashboard/hr-dashboard.component */ 2359);
/* harmony import */ var src_app_user_timesheet_my_timesheet_my_timesheet_component__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! src/app/user-timesheet/my-timesheet/my-timesheet.component */ 10919);
/* harmony import */ var src_app_user_timesheet_team_timesheet_team_timesheet_component__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! src/app/user-timesheet/team-timesheet/team-timesheet.component */ 14965);
/* harmony import */ var src_app_user_timesheet_user_timesheet_component__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! src/app/user-timesheet/user-timesheet.component */ 73206);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! @angular/core */ 37580);









const routes = [{
  path: '',
  component: src_app_user_timesheet_user_timesheet_component__WEBPACK_IMPORTED_MODULE_5__.UserTimesheetComponent,
  children: [{
    path: 'my-timesheet',
    component: src_app_user_timesheet_my_timesheet_my_timesheet_component__WEBPACK_IMPORTED_MODULE_3__.MyTimesheetComponent
  }, {
    path: 'team-timesheet',
    component: src_app_user_timesheet_team_timesheet_team_timesheet_component__WEBPACK_IMPORTED_MODULE_4__.TeamTimesheetComponent
  }, {
    path: 'biomax-request',
    component: src_app_user_timesheet_biomax_approval_biomax_approval_component__WEBPACK_IMPORTED_MODULE_0__.BiomaxApprovalComponent
  }, {
    path: 'hr-dashboard',
    component: src_app_user_timesheet_hr_dashboard_hr_dashboard_component__WEBPACK_IMPORTED_MODULE_2__.HrDashboardComponent
  }, {
    path: 'calendar-view',
    component: src_app_user_timesheet_calendar_view_calendar_view_component__WEBPACK_IMPORTED_MODULE_1__.CalendarViewComponent
  }]
}];
class UserTimesheetRoutingModule {
  static {
    this.ɵfac = function UserTimesheetRoutingModule_Factory(__ngFactoryType__) {
      return new (__ngFactoryType__ || UserTimesheetRoutingModule)();
    };
  }
  static {
    this.ɵmod = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_6__["ɵɵdefineNgModule"]({
      type: UserTimesheetRoutingModule
    });
  }
  static {
    this.ɵinj = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_6__["ɵɵdefineInjector"]({
      imports: [_angular_router__WEBPACK_IMPORTED_MODULE_7__.RouterModule.forChild(routes), _angular_router__WEBPACK_IMPORTED_MODULE_7__.RouterModule]
    });
  }
}
(function () {
  (typeof ngJitMode === "undefined" || ngJitMode) && _angular_core__WEBPACK_IMPORTED_MODULE_6__["ɵɵsetNgModuleScope"](UserTimesheetRoutingModule, {
    imports: [_angular_router__WEBPACK_IMPORTED_MODULE_7__.RouterModule],
    exports: [_angular_router__WEBPACK_IMPORTED_MODULE_7__.RouterModule]
  });
})();

/***/ })

}]);
//# sourceMappingURL=src_app_module-routing_user-timesheet_user-timesheet_module_ts.js.map