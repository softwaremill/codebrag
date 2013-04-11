angular.module('codebrag.session')

    .controller('SessionCtrl', function SessionCtrl($scope, $rootScope, authService, $state) {

        $scope.user = {
            login: '',
            password: '',
            rememberme: false
        };

        $scope.login = function () {
            if (loginFormValid()) {
                logInUser();
                clearPasswordField();
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
                $state.transitionTo('home');
            });
        };

        function clearPasswordField() {
            $scope.loginForm.password.$dirty = false;
            $scope.user.password = '';
        }

        function loginFormValid() {
            // set dirty to show error messages on empty fields when submit is clicked
            $scope.loginForm.login.$dirty = true;
            $scope.loginForm.password.$dirty = true;
            return $scope.loginForm.$invalid === false
        }

        function logInUser() {
            authService.login($scope.user).then(null, function (response) {
                if (response.status === 401) {
                    $rootScope.$broadcast('codebrag:httpAuthError', {status: 401, text: 'Invalid credentials'})
                }
            });
        }

    });