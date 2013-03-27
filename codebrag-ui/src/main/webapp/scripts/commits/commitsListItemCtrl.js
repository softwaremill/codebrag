angular.module('codebrag.commits')

    .controller('CommitsListItemCtrl', function($scope, currentCommit) {

        $scope.openCommitDetails = function(id) {
            currentCommit.id = id;
            $scope.$parent.detailsSection.templateName = "views/commitDetails.html?id=" + id;
        }

    });