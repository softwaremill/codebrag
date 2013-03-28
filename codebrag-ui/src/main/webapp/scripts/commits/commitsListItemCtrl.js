angular.module('codebrag.commits')

    .controller('CommitsListItemCtrl', function($scope, currentCommit) {

        $scope.openCommitDetails = function(commit) {
            currentCommit.id = commit.id
            currentCommit.sha = commit.sha
            $scope.$parent.detailsSection.templateName = "views/commitDetails.html?id=" + commit.id;
        }

    });