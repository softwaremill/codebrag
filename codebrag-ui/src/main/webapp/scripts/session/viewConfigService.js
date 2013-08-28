angular.module('codebrag.session')

    .factory('viewConfigService', function ($http) {

        return {
            fetchConfig: function () {
                return $http.get('rest/view-config/');
            }
        };

    });
