angular.module('codebrag.profile')

    .service('userSettingsService', function($timeout, $q, $http) {

        this.load = function() {
            return $http.get('rest/config/user').then(function(response) {
                return response.data;
            });
        };

        this.save = function(settings) {
            return $http.put('rest/config/user', settings);
        };

    });