angular.module('codebrag.session')

    .controller('SessionCtrl', function SessionCtrl($scope, $rootScope, authService, configService, $state, events, $window, $location, Flash, popupsService, $stateParams) {

        $scope.flash = new Flash();

        showRegisteredSuccessMessage($scope.flash);

        $scope.user = {
            login: '',
            password: '',
            rememberme: false
        };

        $scope.loggedInUser = authService.loggedInUser;

        $scope.login = function () {
            if ($scope.registrationSuccess) {
                $scope.registrationSuccess = false;
            }
            if (loginFormValid()) {
                logInUser();
            }
        };

        $scope.logout = function () {
            authService.logout().then(function () {
                $window.location = '/';
            });
        };

        $scope.openContactFormPopup = function () {
            $rootScope.$broadcast('openContactFormPopup');
        };

        $scope.openInviteFormPopup = function () {
            $rootScope.$broadcast('openInviteFormPopup');
        };

        $scope.openUserProfilePopup = function () {
            $rootScope.$broadcast('openUserProfilePopup');
        };

        $scope.openUserMgmtPopup = function () {
            $rootScope.$broadcast('openUserMgmtPopup');
        };

        $scope.openAboutPopup = popupsService.openAboutPopup;
        $scope.openInvitePopup = popupsService.openInvitePopup;

        function clearPasswordField() {
            $scope.loginForm.password.$dirty = false;
            $scope.user.password = '';
        }

        function clearLoginField() {
            $scope.loginForm.login.$dirty = false;
            $scope.user.login = '';
        }

        function loginFormValid() {
            // set dirty to show error messages on empty fields when submit is clicked
            $scope.loginForm.login.$dirty = true;
            $scope.loginForm.password.$dirty = true;
            return $scope.loginForm.$invalid === false;
        }

        function logInUser() {
            $scope.flash.clear();
            authService.login($scope.user).then(function () {
                clearLoginField();
                clearPasswordField();
            }, function (errors) {
                $scope.flash.addAll('error', errors);
                clearPasswordField();
            });
        }

        function showRegisteredSuccessMessage(flash) {
            // a bit hacky, can't think of good way to pass "registered" flag to not mess up with further urls
            if(/.*\?registered$/.test($location.absUrl())) {
                flash.add("info", "Registration was successful! You can now log in.");
                $location.url($location.path());
            }
        }

    });