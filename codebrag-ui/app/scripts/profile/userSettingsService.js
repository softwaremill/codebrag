angular.module('codebrag.profile')

    // TODO: replace stubbed stuff with real $http calls

    .service('userSettingsService', function($timeout, $q, $http) {

        this.load = function() {

//            return $http.get('rest/config/user').then(function(response) {
//                return response.data;
//            });

            var dfd = $q.defer();
            $timeout(function() {
                var settings = {
                    emailNotifications: false
                };
                dfd.resolve(settings)
            }, 0);
            return dfd.promise;
        };

        this.save = function(settings) {

//            return $http.put('rest/config/user', settings);

            var dfd = $q.defer();
            $timeout(function() {
                _.random(1, 10) % 2 == 0 ? dfd.resolve() : dfd.reject();
            }, 1000);
            return dfd.promise;
        };

    });