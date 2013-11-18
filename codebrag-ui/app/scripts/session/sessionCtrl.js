angular.module('codebrag.session')

    .controller('SessionCtrl', function SessionCtrl($scope, $rootScope, authService, configService, $state, events, $window, $location, flash, popupsService) {

        $scope.user = {
            login: '',
            password: '',
            rememberme: false
        };

        $scope.flash = flash;

        $scope.login = function () {
            if ($scope.registrationSuccess) {
                $scope.registrationSuccess = false;
            }
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
                $rootScope.$broadcast(events.resetNotifications);
                $state.transitionTo('home');
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

        $scope.openAboutPopup = popupsService.openAboutPopup;

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
            delete $scope.loginFailed;
            delete $rootScope.registerSuccessful;
            authService.login($scope.user).then(function () {
                clearLoginField();
                clearPasswordField();
                goToCommitsListIfDirectLogin();
            }, function () {
                clearPasswordField();
                $scope.loginFailed = true;
            });
        }

        function goToCommitsListIfDirectLogin() {
            if ($state.current.name === 'home') {
                $state.transitionTo('commits.list');
            }
        }

    });