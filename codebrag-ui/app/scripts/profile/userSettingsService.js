angular.module('codebrag.profile')

    .service('userSettingsService', function($timeout, $q) {

        this.load = function() {
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
            var dfd = $q.defer();
            $timeout(function() {
                _.random(1, 10) % 2 == 0 ? dfd.resolve() : dfd.reject();
            }, 1000);
            return dfd.promise;
        };

    });