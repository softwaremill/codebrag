angular.module('codebrag.licence')

    .service('licenceRegistrationService', function($http, $q, $rootScope, events) {

        this.registerKey = function(keyToRegister) {
            var payload = {licenceKey: keyToRegister};
            return $http.put('rest/licence', payload).then(registrationSuccess, registrationFailed);
        };

        function registrationSuccess(response) {
            $rootScope.$broadcast(events.licence.licenceKeyRegistered);
            return response.data;
        }

        function registrationFailed(response) {
            return $q.reject(response.data.error);
        }

    });