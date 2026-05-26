"use strict";
(self["webpackChunkemployee_portal_revamp"] = self["webpackChunkemployee_portal_revamp"] || []).push([["src_app_module-routing_user-exit_user-exit_module_ts"],{

/***/ 10765:
/*!**************************************************************!*\
  !*** ./src/app/module-routing/user-exit/user-exit.module.ts ***!
  \**************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   UserExitModule: () => (/* binding */ UserExitModule)
/* harmony export */ });
/* harmony import */ var _angular_common__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/common */ 84460);
/* harmony import */ var _user_exit_routing_module__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./user-exit-routing.module */ 89580);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ 37580);



class UserExitModule {
  static {
    this.ɵfac = function UserExitModule_Factory(__ngFactoryType__) {
      return new (__ngFactoryType__ || UserExitModule)();
    };
  }
  static {
    this.ɵmod = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵdefineNgModule"]({
      type: UserExitModule
    });
  }
  static {
    this.ɵinj = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵdefineInjector"]({
      imports: [_angular_common__WEBPACK_IMPORTED_MODULE_2__.CommonModule, _user_exit_routing_module__WEBPACK_IMPORTED_MODULE_0__.UserExitRoutingModule]
    });
  }
}
(function () {
  (typeof ngJitMode === "undefined" || ngJitMode) && _angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵsetNgModuleScope"](UserExitModule, {
    imports: [_angular_common__WEBPACK_IMPORTED_MODULE_2__.CommonModule, _user_exit_routing_module__WEBPACK_IMPORTED_MODULE_0__.UserExitRoutingModule]
  });
})();

/***/ }),

/***/ 89580:
/*!**********************************************************************!*\
  !*** ./src/app/module-routing/user-exit/user-exit-routing.module.ts ***!
  \**********************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   UserExitRoutingModule: () => (/* binding */ UserExitRoutingModule)
/* harmony export */ });
/* harmony import */ var _angular_router__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! @angular/router */ 18431);
/* harmony import */ var src_app_user_exit_my_resignation_my_resignation_component__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! src/app/user-exit/my-resignation/my-resignation.component */ 23547);
/* harmony import */ var src_app_user_exit_resignation_resignation_component__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! src/app/user-exit/resignation/resignation.component */ 38117);
/* harmony import */ var src_app_user_exit_user_exit_component__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! src/app/user-exit/user-exit.component */ 22812);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! @angular/core */ 37580);






const routes = [{
  path: '',
  component: src_app_user_exit_user_exit_component__WEBPACK_IMPORTED_MODULE_2__.UserExitComponent,
  children: [{
    path: 'my-resignation',
    component: src_app_user_exit_my_resignation_my_resignation_component__WEBPACK_IMPORTED_MODULE_0__.MyResignationComponent
  }, {
    path: 'my-resignation/:id',
    component: src_app_user_exit_my_resignation_my_resignation_component__WEBPACK_IMPORTED_MODULE_0__.MyResignationComponent
  }, {
    path: 'resignation',
    component: src_app_user_exit_resignation_resignation_component__WEBPACK_IMPORTED_MODULE_1__.ResignationComponent
  }]
}];
class UserExitRoutingModule {
  static {
    this.ɵfac = function UserExitRoutingModule_Factory(__ngFactoryType__) {
      return new (__ngFactoryType__ || UserExitRoutingModule)();
    };
  }
  static {
    this.ɵmod = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_3__["ɵɵdefineNgModule"]({
      type: UserExitRoutingModule
    });
  }
  static {
    this.ɵinj = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_3__["ɵɵdefineInjector"]({
      imports: [_angular_router__WEBPACK_IMPORTED_MODULE_4__.RouterModule.forChild(routes), _angular_router__WEBPACK_IMPORTED_MODULE_4__.RouterModule]
    });
  }
}
(function () {
  (typeof ngJitMode === "undefined" || ngJitMode) && _angular_core__WEBPACK_IMPORTED_MODULE_3__["ɵɵsetNgModuleScope"](UserExitRoutingModule, {
    imports: [_angular_router__WEBPACK_IMPORTED_MODULE_4__.RouterModule],
    exports: [_angular_router__WEBPACK_IMPORTED_MODULE_4__.RouterModule]
  });
})();

/***/ })

}]);
//# sourceMappingURL=src_app_module-routing_user-exit_user-exit_module_ts.js.map