angular.module('codebrag.commits')

    .controller('CommitDetailsCtrl', function ($stateParams, $state, $scope, commitsListService) {

        var commitId = $stateParams.id;

        commitsListService.loadCommitById(commitId).then(function(commit) {
            $scope.currentCommit = commit;
            $scope.currentCommitReactions = new codebrag.CommitReactions(commit.reactions, commit.lineReactions)
            angular.noop();
        });

        $scope.markCurrentCommitAsReviewed = function() {
            commitsListService.removeCommitAndGetNext(commitId).then(function(nextCommit) {
                goTo(nextCommit);
            })
        };

        function goTo(nextCommit) {
            if (_.isNull(nextCommit)) {
                $state.transitionTo('commits.list');
            } else {
                $state.transitionTo('commits.details', {id: nextCommit.id});
            }
        }

    });


