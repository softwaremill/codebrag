angular.module('codebrag.session')

    .factory('configService', function ($http, $q) {

        var config;

        return {
            fetchConfig: function () {
                if(config) {
                    return $q.when(config);
                }
                return $http.get('rest/config/').then(function(resp) {
                    config = resp.data;
                    return config;
                });
            }
        };

    });