angular.module('codebrag.profile')

    .service('userSettingsService', function($timeout, $q, $http, authService) {

        this.load = function() {
            return $http.get('rest/users/settings').then(function(response) {
                return response.data.userSettings;
            });
        };

        this.save = function(settings) {
            return $http.put('rest/users/settings', settings).then(function(response) {
                angular.extend(authService.loggedInUser.settings, response.data.userSettings);
                return response.data.userSettings;
            });
        };

    });