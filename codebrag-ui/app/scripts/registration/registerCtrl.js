angular.module('codebrag.registration')

    .factory('registrationWizard', function() {

        return {
            invitationCode: null,
            registeredUser: null,
            signupVisible: function() {
                return Boolean(this.invitationCode && !this.registeredUser)
            },
            watchVisible: function() {
                return Boolean(this.invitationCode && this.registeredUser);
            }
        };

    })

    .controller('RegistrationWizardCtrl', function($scope, registrationWizard, invitationId, RepoBranch) {

        $scope.wizard = registrationWizard;
        registrationWizard.invitationCode = invitationId;

        $scope.selectedBranch = 'feature';
        $scope.branches = [
            new RepoBranch({ branchName: 'master', watching: false}),
            new RepoBranch({ branchName: 'feature', watching: false}),
            new RepoBranch({ branchName: 'bugfix', watching: true}),
            new RepoBranch({ branchName: 'v2.0', watching: true})
        ];

        $scope.selectedRepo = 'codebrag';

        $scope.repos = ['codebrag', 'bootzooka', 'codebrag-website'];

        $scope.selectRepo = function(repo) {
            console.log('repo selected:', repo);
        };

        $scope.selectBranch = function(branch) {
            console.log('branch selected:', branch);
        };

    })

    .controller('WatchCtrl', function($scope, registrationWizard) {

        $scope.wizard = registrationWizard;

    })

    .controller('RegisterCtrl', function RegisterCtrl($scope, $rootScope, registerService, Flash, registrationWizard) { // TODO: refactor and cleanup "flashes"

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
            registerService.register($scope.user).then(signupSuccess, signupError)
        }

        function signupSuccess(data) {
            clearForm();
            $rootScope.registeredUser = data;
        }

        function signupError(errors) {
            $scope.flash.addAll('error', errors);
        }

        $scope.next = function() {
            registrationWizard.registeredUser = { login: 'fake', id: 123 };
        }
    });