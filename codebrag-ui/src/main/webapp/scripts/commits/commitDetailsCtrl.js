angular.module('codebrag.commits')

    .controller('CommitDetailsCtrl', function ($stateParams, $state, $scope, commitsListService) {

        var commitId = $stateParams.id;

        commitsListService.loadCommitDetails(commitId).then(function(commit) {
            $scope.currentCommit = new codebrag.CurrentCommit(commit);
        });

        $scope.markCurrentCommitAsReviewed = function() {
            commitsListService.makeReviewedAndGetNext(commitId).then(function(nextCommit) {
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


