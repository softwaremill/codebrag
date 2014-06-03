angular.module('codebrag.auth')

    .factory('registerService', function($http, $state, $stateParams) {

        var registrationApiUrl = 'rest/users/register',
            registerService;

        registerService = {
            register: function(user) {
                user.invitationCode = $stateParams.invitationId;
                var registerRequest = $http.post(registrationApiUrl, user, {unique: true, requestId: 'register'});
                return registerRequest.then(function() {
                    $state.transitionTo('home');
                });
            }
        };

        return registerService;
    });