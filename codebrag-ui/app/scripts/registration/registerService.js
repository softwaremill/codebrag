angular.module('codebrag.registration')

    .factory('registerService', function($http, $q) {

        var registrationApiUrl = 'rest/users/register',
            registerService;

        function success(response) {
            return response.data;
        }

        function error(response) {
            return $q.reject(response.data.errors);
        }

        registerService = {
            register: function(userData, invitationCode) {
                userData.invitationCode = invitationCode;
                return $http.post(registrationApiUrl, userData, {unique: true, requestId: 'register'}).then(success, error);
            }
        };

        return registerService;
    });