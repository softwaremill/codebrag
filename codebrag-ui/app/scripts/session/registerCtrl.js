angular.module('codebrag.session')
    .controller('RegisterCtrl', function RegisterCtrl($scope, $rootScope, registerService, flash, Flash) { // TODO: refactor and cleanup "flashes"

        $scope.flash = new Flash();

        $scope.user = {
            login: '',
            email: '',
            password: '',
            repassword: ''
        };

        $scope.register = function () {
            if (registerFormValid()) {
                registerUser();
            }
        };

        function registerFormValid() {
            // set dirty to show error messages on empty fields when submit is clicked
            $scope.registerForm.login.$dirty = true;
            $scope.registerForm.email.$dirty = true;
            $scope.registerForm.password.$dirty = true;
            $scope.registerForm.repassword.$dirty = true;

            return $scope.registerForm.$valid && !$scope.registerForm.repassword.$error.repeat;
        }

        function clearForm() {
            $scope.registerForm.login.$dirty = false;
            $scope.registerForm.email.$dirty = false;
            $scope.registerForm.password.$dirty = false;
            $scope.registerForm.repassword.$dirty = false;

            $scope.user.login = '';
            $scope.user.email = '';
            $scope.user.password = '';
            $scope.user.repassword = '';
        }

        function registerUser() {
            $scope.flash.clear();
            registerService.register($scope.user).then(function() {
                clearForm();
                flash.set("Registration was successful! You can now log in.");
            }, function (errorResponse) {
                if(errorResponse.status !== 499) {
                    $scope.flash.addAll('error', errorResponse.data);
                }
            });
        }
    });