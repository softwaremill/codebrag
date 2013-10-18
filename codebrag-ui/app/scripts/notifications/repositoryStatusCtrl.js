angular.module('codebrag.notifications')

    .controller('RepositoryStatusCtrl', function ($scope, $modalInstance, statusData) {
        $scope.statusData = statusData;

        $scope.close = function() {
            $modalInstance.close();
        }
    });