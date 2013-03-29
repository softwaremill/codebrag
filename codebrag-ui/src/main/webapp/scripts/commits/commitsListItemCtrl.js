angular.module('codebrag.commits')

    .controller('CommitsListItemCtrl', function($scope, currentCommit) {

        $scope.openCommitDetails = function(commit) {
            currentCommit.id = commit.id
            currentCommit.sha = commit.sha
            if($scope.$parent && $scope.$parent.detailsSection) {
                $scope.$parent.detailsSection.templateName = "views/commitDetails.html?id=" + commit.id;
            }
        }

    });