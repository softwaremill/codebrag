angular.module('codebrag.session')

    .directive('showOnDemo', function(configService) {
        return {
            restrict: 'A',
            scope: {},
            link: function(scope, el) {
                el.hide();
                configService.fetchConfig().then(function(config) {
                    !!config.demo ? el.show() : el.hide();
                });
            }
        }
    })

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