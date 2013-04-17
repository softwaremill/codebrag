angular.module('codebrag.commits')

    .controller('CommitsListItemCtrl', function($state, $scope, PendingCommits) {

        $scope.openCommitDetails = function(commit) {
            $state.transitionTo('commits.details', {id: commit.id});
        }

        $scope.markAsReviewed = function(commit, $event) {
            $event.stopPropagation();
            PendingCommits.delete({id: commit.id}, function() {
                console.log('should be removed');
                var itemIndex = $scope.commits.indexOf(commit);
                $scope.commits.splice(itemIndex, 1);
            })
        }

    });