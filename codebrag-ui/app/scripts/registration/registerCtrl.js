angular.module('codebrag.registration')

    .controller('RegisterCtrl', function RegisterCtrl($scope, registerService, Flash, registrationWizardData) {

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

        $scope.fakeRegister = function() {
            registrationWizardData.registeredUser = { id: '123123', login: 'john_doe', email: 'john@codebrag.com'};
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
            registerService.register($scope.user, registrationWizardData.invitationCode).then(signupSuccess, signupError)
        }

        function signupSuccess(data) {
            clearForm();
            registrationWizardData.registeredUser = data;
        }

        function signupError(errors) {
            $scope.flash.addAll('error', errors);
        }

    });