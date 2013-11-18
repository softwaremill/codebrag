angular.module('codebrag.profile')

    .controller('AboutCtrl', function($scope, $http) {

        $http.get('rest/version').then(exposeVersionNumber);

        function exposeVersionNumber(response) {
            $scope.version = response.data.version;
        }

    });
