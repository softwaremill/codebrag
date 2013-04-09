angular.module('codebrag.session')

    .controller('SessionCtrl', function SessionCtrl($scope, authService, $state, $stateParams) {

        $scope.user = {
            login: '',
            password: '',
            rememberme: false
        };

        $scope.login = function () {
            // set dirty to show error messages on empty fields when submit is clicked
            $scope.loginForm.login.$dirty = true;
            $scope.loginForm.password.$dirty = true;

            if ($scope.loginForm.$invalid === false) {
                authService.login($scope.user).then(loginSuccess, loginFailed);
            }

            function loginSuccess () {
                // TODO: handle optionalRedir
                $state.transitionTo('home');
            }

            function loginFailed() {
                showErrorMessage("Invalid login and/or password.");
            }
        };

        $scope.isLogged = function () {
            return authService.isAuthenticated();
        };

        $scope.isNotLogged = function () {
            return authService.isNotAuthenticated();
        };

        $scope.getLoggedUserName = function () {
            if(authService.isAuthenticated()) {
                return authService.loggedInUser.login;
            }
        };

        $scope.logout = function () {
            authService.logout().then(function (data) {
                $state.transitionTo('login');
            });
        };

    });