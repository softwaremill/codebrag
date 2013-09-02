angular.module('codebrag.session')

    .factory('configService', function ($http) {

        return {
            fetchConfig: function () {
                return $http.get('rest/config/');
            }
        };

    });
