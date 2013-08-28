angular.module('codebrag.session')

    .controller('SessionCtrl', function SessionCtrl($scope, $rootScope, authService, viewConfigService, $state, events, $window, $location, flash) {

        $scope.user = {
            login: '',
            password: '',
            rememberme: false
        };

        $scope.flash = flash;

        viewConfigService.fetchConfig().success(function (response) {
            $scope.demo = response.demo;
        });

        $scope.login = function () {
            if (loginFormValid()) {
                logInUser();
            }
        };

        $scope.githubLogin = function () {
            var githubLoginUrl = '/rest/github/authenticate';
            $window.location.href = githubLoginUrl + '?redirectTo=' + $location.url();
        };

        $scope.isLogged = function () {
            return authService.isAuthenticated();
        };

        $scope.isNotLogged = function () {
            return authService.isNotAuthenticated();
        };

        $scope.loggedInUser = function () {
            if (!authService.isAuthenticated()) {
                return {};
            }
            return authService.loggedInUser;
        };

        $scope.logout = function () {
            authService.logout().then(function () {
                $state.transitionTo('home');
            });
        };

        $scope.openContactFormPopup = function () {
            $rootScope.$broadcast('openContactFormPopup');
        };

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
            return $scope.loginForm.$invalid === false
        }

        function logInUser() {
            delete $scope.loginFailed;
            delete $rootScope.registerSuccessful;
            authService.login($scope.user).then(function () {
                clearLoginField();
                clearPasswordField();
            }, function (errorResponse) {
                clearPasswordField();
                $scope.loginFailed = true;
            });
        }

    });