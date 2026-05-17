"use strict";
(self["webpackChunkemployee_portal_revamp"] = self["webpackChunkemployee_portal_revamp"] || []).push([["src_app_module-routing_user-leaves_user-leaves_module_ts"],{

/***/ 36461:
/*!******************************************************************!*\
  !*** ./src/app/module-routing/user-leaves/user-leaves.module.ts ***!
  \******************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   UserLeavesModule: () => (/* binding */ UserLeavesModule)
/* harmony export */ });
/* harmony import */ var _angular_common__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/common */ 84460);
/* harmony import */ var _user_leaves_routing_module__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./user-leaves-routing.module */ 74444);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ 37580);



class UserLeavesModule {
  static {
    this.ɵfac = function UserLeavesModule_Factory(__ngFactoryType__) {
      return new (__ngFactoryType__ || UserLeavesModule)();
    };
  }
  static {
    this.ɵmod = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵdefineNgModule"]({
      type: UserLeavesModule
    });
  }
  static {
    this.ɵinj = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵdefineInjector"]({
      imports: [_angular_common__WEBPACK_IMPORTED_MODULE_2__.CommonModule, _user_leaves_routing_module__WEBPACK_IMPORTED_MODULE_0__.UserLeavesRoutingModule]
    });
  }
}
(function () {
  (typeof ngJitMode === "undefined" || ngJitMode) && _angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵsetNgModuleScope"](UserLeavesModule, {
    imports: [_angular_common__WEBPACK_IMPORTED_MODULE_2__.CommonModule, _user_leaves_routing_module__WEBPACK_IMPORTED_MODULE_0__.UserLeavesRoutingModule]
  });
})();

/***/ }),

/***/ 74444:
/*!**************************************************************************!*\
  !*** ./src/app/module-routing/user-leaves/user-leaves-routing.module.ts ***!
  \**************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   UserLeavesRoutingModule: () => (/* binding */ UserLeavesRoutingModule)
/* harmony export */ });
/* harmony import */ var _angular_router__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! @angular/router */ 18431);
/* harmony import */ var src_app_user_leaves_comp_off_comp_off_component__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! src/app/user-leaves/comp-off/comp-off.component */ 50329);
/* harmony import */ var src_app_user_leaves_holidays_holidays_component__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! src/app/user-leaves/holidays/holidays.component */ 36965);
/* harmony import */ var src_app_user_leaves_leave_leave_component__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! src/app/user-leaves/leave/leave.component */ 54265);
/* harmony import */ var src_app_user_leaves_user_leaves_component__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! src/app/user-leaves/user-leaves.component */ 82596);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! @angular/core */ 37580);







const routes = [{
  path: '',
  component: src_app_user_leaves_user_leaves_component__WEBPACK_IMPORTED_MODULE_3__.UserLeavesComponent,
  children: [{
    path: 'leave',
    component: src_app_user_leaves_leave_leave_component__WEBPACK_IMPORTED_MODULE_2__.LeaveComponent
  }, {
    path: 'holiday',
    component: src_app_user_leaves_holidays_holidays_component__WEBPACK_IMPORTED_MODULE_1__.HolidaysComponent
  }, {
    path: 'compOff',
    component: src_app_user_leaves_comp_off_comp_off_component__WEBPACK_IMPORTED_MODULE_0__.CompOffComponent
  }]
}];
class UserLeavesRoutingModule {
  static {
    this.ɵfac = function UserLeavesRoutingModule_Factory(__ngFactoryType__) {
      return new (__ngFactoryType__ || UserLeavesRoutingModule)();
    };
  }
  static {
    this.ɵmod = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_4__["ɵɵdefineNgModule"]({
      type: UserLeavesRoutingModule
    });
  }
  static {
    this.ɵinj = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_4__["ɵɵdefineInjector"]({
      imports: [_angular_router__WEBPACK_IMPORTED_MODULE_5__.RouterModule.forChild(routes), _angular_router__WEBPACK_IMPORTED_MODULE_5__.RouterModule]
    });
  }
}
(function () {
  (typeof ngJitMode === "undefined" || ngJitMode) && _angular_core__WEBPACK_IMPORTED_MODULE_4__["ɵɵsetNgModuleScope"](UserLeavesRoutingModule, {
    imports: [_angular_router__WEBPACK_IMPORTED_MODULE_5__.RouterModule],
    exports: [_angular_router__WEBPACK_IMPORTED_MODULE_5__.RouterModule]
  });
})();

/***/ })

}]);
//# sourceMappingURL=src_app_module-routing_user-leaves_user-leaves_module_ts.js.map