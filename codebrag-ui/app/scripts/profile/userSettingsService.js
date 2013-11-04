angular.module('codebrag.profile')

    .service('userSettingsService', function($timeout, $q, $http) {

        this.load = function() {
            return $http.get('rest/users/settings').then(function(response) {
                return response.data.userSettings;
            });
        };

        this.save = function(settings) {
            return $http.put('rest/users/settings', settings);
        };

    });