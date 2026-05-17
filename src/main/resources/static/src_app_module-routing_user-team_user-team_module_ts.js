"use strict";
(self["webpackChunkemployee_portal_revamp"] = self["webpackChunkemployee_portal_revamp"] || []).push([["src_app_module-routing_user-team_user-team_module_ts"],{

/***/ 33888:
/*!**********************************************************************!*\
  !*** ./src/app/module-routing/user-team/user-team-routing.module.ts ***!
  \**********************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   UserTeamRoutingModule: () => (/* binding */ UserTeamRoutingModule)
/* harmony export */ });
/* harmony import */ var _angular_router__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! @angular/router */ 18431);
/* harmony import */ var src_app_user_team_my_team_my_team_component__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! src/app/user-team/my-team/my-team.component */ 69492);
/* harmony import */ var src_app_user_team_resource_management_resource_management_component__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! src/app/user-team/resource-management/resource-management.component */ 23984);
/* harmony import */ var src_app_user_team_team_config_team_config_component__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! src/app/user-team/team-config/team-config.component */ 77336);
/* harmony import */ var src_app_user_team_team_member_team_member_component__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! src/app/user-team/team-member/team-member.component */ 72472);
/* harmony import */ var src_app_user_team_user_team_component__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! src/app/user-team/user-team.component */ 55256);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! @angular/core */ 37580);








const routes = [{
  path: '',
  component: src_app_user_team_user_team_component__WEBPACK_IMPORTED_MODULE_4__.UserTeamComponent,
  children: [{
    path: 'my-team',
    component: src_app_user_team_my_team_my_team_component__WEBPACK_IMPORTED_MODULE_0__.MyTeamComponent
  }, {
    path: 'team-member',
    component: src_app_user_team_team_member_team_member_component__WEBPACK_IMPORTED_MODULE_3__.TeamMemberComponent
  }, {
    path: 'team-config',
    component: src_app_user_team_team_config_team_config_component__WEBPACK_IMPORTED_MODULE_2__.TeamConfigComponent
  }, {
    path: 'resource-management',
    component: src_app_user_team_resource_management_resource_management_component__WEBPACK_IMPORTED_MODULE_1__.ResourceManagementComponent
  }, {
    path: 'resource-management/:id',
    component: src_app_user_team_resource_management_resource_management_component__WEBPACK_IMPORTED_MODULE_1__.ResourceManagementComponent
  }]
}];
class UserTeamRoutingModule {
  static {
    this.ɵfac = function UserTeamRoutingModule_Factory(__ngFactoryType__) {
      return new (__ngFactoryType__ || UserTeamRoutingModule)();
    };
  }
  static {
    this.ɵmod = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵdefineNgModule"]({
      type: UserTeamRoutingModule
    });
  }
  static {
    this.ɵinj = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵdefineInjector"]({
      imports: [_angular_router__WEBPACK_IMPORTED_MODULE_6__.RouterModule.forChild(routes), _angular_router__WEBPACK_IMPORTED_MODULE_6__.RouterModule]
    });
  }
}
(function () {
  (typeof ngJitMode === "undefined" || ngJitMode) && _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵsetNgModuleScope"](UserTeamRoutingModule, {
    imports: [_angular_router__WEBPACK_IMPORTED_MODULE_6__.RouterModule],
    exports: [_angular_router__WEBPACK_IMPORTED_MODULE_6__.RouterModule]
  });
})();

/***/ }),

/***/ 49873:
/*!**************************************************************!*\
  !*** ./src/app/module-routing/user-team/user-team.module.ts ***!
  \**************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   UserTeamModule: () => (/* binding */ UserTeamModule)
/* harmony export */ });
/* harmony import */ var _angular_common__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/common */ 84460);
/* harmony import */ var _user_team_routing_module__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./user-team-routing.module */ 33888);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ 37580);



class UserTeamModule {
  static {
    this.ɵfac = function UserTeamModule_Factory(__ngFactoryType__) {
      return new (__ngFactoryType__ || UserTeamModule)();
    };
  }
  static {
    this.ɵmod = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵdefineNgModule"]({
      type: UserTeamModule
    });
  }
  static {
    this.ɵinj = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵdefineInjector"]({
      imports: [_angular_common__WEBPACK_IMPORTED_MODULE_2__.CommonModule, _user_team_routing_module__WEBPACK_IMPORTED_MODULE_0__.UserTeamRoutingModule]
    });
  }
}
(function () {
  (typeof ngJitMode === "undefined" || ngJitMode) && _angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵsetNgModuleScope"](UserTeamModule, {
    imports: [_angular_common__WEBPACK_IMPORTED_MODULE_2__.CommonModule, _user_team_routing_module__WEBPACK_IMPORTED_MODULE_0__.UserTeamRoutingModule]
  });
})();

/***/ })

}]);
//# sourceMappingURL=src_app_module-routing_user-team_user-team_module_ts.js.map