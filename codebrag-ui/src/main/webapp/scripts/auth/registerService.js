angular.module('codebrag.auth')

    .factory('registerService', function($http, $state) {

        var registerService = {

            register: function(user) {
                var registerRequest = $http.post('rest/users/register', user);
                return registerRequest.then(function(response) {
                    $state.transitionTo('home');
                });
            }
        };

        return registerService;
    });