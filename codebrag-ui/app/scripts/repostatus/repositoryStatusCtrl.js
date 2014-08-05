angular.module('codebrag.repostatus')

    .controller('RepositoryStatusCtrl', function ($scope, $modalInstance, repoStatus) {
        $scope.repoStatus = repoStatus;

        $scope.close = function() {
            $modalInstance.close();
        }
    });