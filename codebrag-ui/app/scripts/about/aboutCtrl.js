angular.module('codebrag.profile')

    .controller('AboutCtrl', function($scope, $http) {

        $http.get('rest/version').then(exposeVersionNumber);

        function exposeVersionNumber(response) {
            $scope.buildInfo = response.data
        }

    });
