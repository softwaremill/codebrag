angular.module('codebrag.auth')

    .factory('registerService', function($http, $state, $stateParams) {

        var registerService = {

            register: function(user) {
                user.invitationCode = $stateParams.invitationId;
                var registerRequest = $http.post('rest/users/register', user, {unique: true, requestId: 'register'});
                return registerRequest.then(function() {
                    $state.transitionTo('home');
                });
            }
        };

        return registerService;
    });