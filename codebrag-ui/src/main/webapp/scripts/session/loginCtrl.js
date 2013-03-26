angular.module('codebrag.session')

    .controller('SessionCtrl', function SessionCtrl($scope, authService, $location, $routeParams) {

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
                var optionalRedir = $routeParams.page;
                if (typeof optionalRedir !== "undefined") {
                    $location.search("page", null);
                    $location.path(optionalRedir);
                } else {
                    $location.path("");
                }
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
                $location.path("");
            });
        };

    });