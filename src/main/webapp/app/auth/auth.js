/*  Copyright 2014 Danish Maritime Authority.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

//  The techniques applied in this file follows the suggestions in
//
//  "Techniques for authentication in AngularJS applications" 
//  
//  (https://medium.com/opinionated-angularjs/techniques-for-authentication-in-angularjs-applications-7bbf0346acec)

angular.module('mcp.auth', ['ui.bootstrap', 'http-auth-interceptor', 'ngStorage'])


    /* Constants */

    // Authentication events broadcasted on rootScope
    .constant('AUTH_EVENTS', {
      loginSuccess: 'auth-login-success',
      loginFailed: 'auth-login-failed',
      loginCancelled: 'auth-login-cancelled',
      logoutSuccess: 'auth-logout-success',
      sessionTimeout: 'auth-session-timeout',
      notAuthenticated: 'auth-not-authenticated',
      notAuthorized: 'auth-not-authorized'
    })

    .constant('USER_ROLES', {
      all: '*',
      admin: 'ADMIN',
      user: 'USER',
      editor: 'editor',
      guest: 'guest'
    })


    /* Controllers */

    // A container for global application logic
    .controller('ApplicationController', function ($rootScope, $scope, $modal, $location, $localStorage,
        USER_ROLES, AUTH_EVENTS, AuthService, httpAuthInterceptorService, Session, UserContext, OrganizationContext) {
      $scope.sidebar = {isMinified: false};
      $scope.userRoles = USER_ROLES;
      $scope.isAuthorized = AuthService.isAuthorized;
      $scope.isLoggedIn = AuthService.isAuthenticated;
      $scope.navigationTarget = null;
      $scope.message = null;
      $scope.mcpInfo = mcpInfo; // from "info.js"
      $scope.alertMessages = [];

      // FIXME: Remove this switch once carousels has been fixed
      // HACK: carousel elements stall protractor during e2e testing - disabled it during tests
      $scope.isProtractorTesting = function () {
        return typeof (window.protractorE2EtestIsRunning) !== 'undefined';
      };

      $scope.organizationRef = function (organization) {
        return organization ? organization.primaryAlias || organization.organizationId : "_undefined_";
      };

      // Import user session from local storage (...if any)
      $scope.$storage = $localStorage.$default({userSession: {}});
      $scope.currentUser = Session.user;
      UserContext.setCurrentUser($scope.currentUser);

      // Watch the storage for changes that origins from other instances running 
      // in other windows or tabs
      // (this function is called on reload and whenever storage changes)
      $scope.$watch('$storage.userSession', function () {
        // helper functions
        var userHasLoggedOut = function () {
          // if we had a user but session user is cleared it indicates 
          // that someone logged out from another window
          return prevUser && !$scope.currentUser;
        };
        var userHasChanged = function () {
          // true if a new user has logged in as somebody else without logging 
          // out the previous user first
          return prevUser && prevUser.name !== $scope.currentUser.name;
        };
        // store previous user
        var prevUser = $scope.currentUser;
        // read existing session
        if ($scope.$storage.userSession) {
          Session.importFrom($scope.$storage.userSession);
        }
        // load storage user
        $scope.currentUser = $scope.$storage.userSession.user;
        UserContext.setCurrentUser($scope.currentUser);

        // should reset to landingpage?
        if (userHasLoggedOut() || userHasChanged()) {
          $location.path('/').replace();
        }
      }, true);

      // Login listener that binds the login session to current user upon login success
      $scope.$on(AUTH_EVENTS.loginSuccess, function () {
        //console.log("EVENT: User logged in! ", Session.user, "Session: ", Session);
        $scope.currentUser = Session.user;
        UserContext.setCurrentUser($scope.currentUser);

        if (!$scope.$storage.userSession)
          $scope.$storage.userSession = {};

        // export to storage 
        // (...this will trigger an import from the storage)
        Session.exportTo($scope.$storage.userSession);

        // Process pending requests
        httpAuthInterceptorService.loginConfirmed();
        // Navigate to defered page
        if ($scope.navigationTarget) {
          //console.log("forwarding to defered path: ", $scope.navigationTarget);
          $location.path($scope.navigationTarget).replace();
          $scope.navigationTarget = null;
        }
      });

      // Logout listener that cleans up state bound to the current user
      $scope.$on(AUTH_EVENTS.logoutSuccess, function () {
        //console.log("EVENT: User logged out! Session: ", Session);

        // reset local storage
        $scope.$storage.$reset({userSession: {}});
        Session.importFrom($scope.$storage.userSession);
        $scope.currentUser = Session.user;

        // Clear user- and organization context
        UserContext.reset();
        OrganizationContext.resetCurrentOrganization();

        $scope.navigationTarget = null;
        $location.path('/').replace();
      });

      // Login listener that listens for login failure
      $scope.$on(AUTH_EVENTS.loginFailed, function () {
        console.log("User login failed!");
      });

      // Login listener that resets any pending requests if user cancels login
      $scope.$on(AUTH_EVENTS.loginCancelled, function () {
        //console.log("User login cancelled!");
        httpAuthInterceptorService.loginCancelled();
      });

      // Login listener that warns that user is not authorized for the action
      // ( broadcasted by app.js in case of page-transition to a page that 
      //   the user is not authorized to visit )
      $scope.$on(AUTH_EVENTS.notAuthorized, function () {
        console.log("User not authorized to visit this page!");
        $scope.alertMessages = ["User not authorized!"];
      });

      // Login listener that brings up the login dialog whenver the "event:auth-loginRequired!" event is fired
      $scope.$on('event:auth-loginRequired', function () {
        //console.log("event:auth-loginRequired!");
        $scope.openLoginDialog();
      });

      $scope.$on(AUTH_EVENTS.notAuthenticated, function (event, targetRoute) {
        console.log("AUTH_EVENTS.notAuthenticated targetRoute: ", targetRoute, " event:", event);
        $scope.navigationTarget = targetRoute;
        $scope.openLoginDialog();
      });

      $scope.openLoginDialog = function () {

        if (!$scope.loginDialogOpen) {
          $scope.loginDialogOpen = true;
          $modal.open({
            templateUrl: 'auth/loginDialog.html',
            controller: 'LoginController',
            size: 'sm',
            backdrop: 'static'
          }).result.then(function () {
            // Login dialog closed
            $scope.loginDialogOpen = false;
          }, function () {
            $scope.loginDialogOpen = false;
            // Login dialog dismissed (user pressed CANCEL or ESCAPE)
            $rootScope.$broadcast(AUTH_EVENTS.loginCancelled);
          });
        }
      };

      $scope.logout = function () {
        //console.log("Logging out...");
        AuthService.logout().then(function () {
          $rootScope.$broadcast(AUTH_EVENTS.logoutSuccess);
        }, function () {
          $rootScope.$broadcast(AUTH_EVENTS.logoutFailed);
        });
      };

    })

    .controller('LoginController', ['$scope', '$rootScope', 'AuthService', 'AUTH_EVENTS',
      function ($scope, $rootScope, AuthService, AUTH_EVENTS) {

        $scope.scene = "login";

        resetMessages = function () {
          $scope.message = null;
          $scope.alert = null;
        };

        $scope.show = function (sceneTarget) {
          $scope.scene = sceneTarget;
        };

        $scope.login = function (credentials) {
          resetMessages();
          $scope.loginPromise = AuthService.login(credentials).then(function () {
            $rootScope.$broadcast(AUTH_EVENTS.loginSuccess);
            $scope.$close();
          }, function () {
            $scope.alert = "Incorrect username or password";
            $rootScope.$broadcast(AUTH_EVENTS.loginFailed);
          });
        };

        $scope.sendInstructions = function (email) {
          resetMessages();
          $scope.busyPromise = AuthService.sendForgotPassword(email).then(function () {
            $scope.message = "A mail has been sent to " + email;
            $scope.scene = "login";
          }, function (error) {
            //console.log("Error during send of password instructions: ", error);
            $scope.alert = "Whoops! Something went wrong: (" + error.status + ") " + error.statusText;
          });
        };

      }])


    /* Services */

    // AuthService
    // Service logic related to the remote authentication and authorization 
    .service('AuthService', function ($http, Session) {
      self = this;

      this.login = function (credentials) {
        //console.log("Logging in with " + credentials.username);
        return $http
            .post('/rest/authentication/login', credentials, {ignoreAuthModule: true})
            .then(function (respone) {
              var data = respone.data;
              //console.log("Login response data: ", data);
              Session.create(data.id, data.username, data.roles);
            });
      };

      this.sendForgotPassword = function (email) {
        //console.log("Sending password instructions to  " + email);
        return $http
            .post('/rest/authentication?command=SendResetPasswordInstructions', new SendResetPasswordInstructions(email), {ignoreAuthModule: true})
            .then(function (respone) {
              var data = respone.data;
            });
      };


      this.resetPassword = function (username, verificationId, newPassword) {
        //console.log("Sending change password request", username, verificationId);
        return $http
            .put('/rest/api/users/' + username + '?command=ChangeUserPassword', new ChangeUserPassword('', verificationId, newPassword), {ignoreAuthModule: true})
            .then(function (respone) {
              var data = respone.data;
            });
      };

      this.isAuthenticated = function () {
        return !!Session.userId;
      };

      /* finds the intersection of 
       * two arrays in a simple fashion.  
       *
       * PARAMS
       *  a - first array, must already be sorted
       *  b - second array, must already be sorted
       *
       * NOTES
       *
       *  Should have O(n) operations, where n is 
       *    n = MIN(a.length(), b.length())
       */
      var intersect_safe = function (a, b)
      {
        var ai = 0, bi = 0;
        var result = new Array();

        a.sort();
        b.sort();

        while (ai < a.length && bi < b.length)
        {
          if (a[ai] < b[bi]) {
            ai++;
          }
          else if (a[ai] > b[bi]) {
            bi++;
          }
          else /* they're equal */
          {
            result.push(a[ai]);
            ai++;
            bi++;
          }
        }

        return result;
      };

      var intersects = function (set1, set2) {
        return set1 && set2 && intersect_safe(set1, set2).length;
      };

      /* true, if user possess any of the menthioned roles */
      this.isAuthorized = function (authorizedRoles) {
        if (!angular.isArray(authorizedRoles)) {
          authorizedRoles = [authorizedRoles];
        }
        return self.isAuthenticated() && intersects(Session.userRoles, authorizedRoles);
      };
      this.logout = function () {
        //console.log("Logging out...");
        return $http
            .post('/rest/authentication/logout', {}, {ignoreAuthModule: true})
            .then(function (respone) {
              var data = respone.data;
              //console.log("Logout response data: ", data);
              Session.destroy();
            });
      };

    })

    // Session
    // the user’s session information
    .service('Session', function () {
      this.create = function (sessionId, userId, userRoles) {
        this.id = sessionId;
        this.userId = userId;
        this.userRoles = userRoles;
        this.user = {name: userId, role: userRoles};
      };
      this.importFrom = function (userSession) {
        this.id = userSession.id;
        this.userId = userSession.userId;
        this.userRoles = userSession.userRoles;
        this.user = userSession.user;
      };
      this.exportTo = function (userSession) {
        userSession.id = this.id;
        userSession.userId = this.userId;
        userSession.userRoles = this.userRoles;
        userSession.user = this.user;
      };
      this.destroy = function () {
        this.id = null;
        this.userId = null;
        this.userRoles = null;
        this.user = null;
      };
      return this;
    });
