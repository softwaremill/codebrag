angular.module('codebrag.allfollowups')

    .controller('AllFollowupDetailsCtrl', function ($stateParams, $state, $scope, allfollowupsService, commitsService) {

        var followupId = $stateParams.followupId;

        $scope.scrollTo = $stateParams.commentId;

        allfollowupsService.loadFollowupDetails(followupId).then(function(followup) {
            $scope.currentFollowup = followup;
            commitsService.commitDetails(followup.commit.sha, followup.commit.repoName).then(function(commit) {
                $scope.currentCommit = new codebrag.CurrentCommit(commit);

            });
        });

        $scope.markCurrentFollowupAsDone = function() {
            allfollowupsService.removeAndGetNext(followupId).then(function(nextFollowup) {
                goTo(nextFollowup);
            });
        };

        function goTo(nextFollowup) {
            if (_.isNull(nextFollowup)) {
                $state.transitionTo('allfollowups.list');
            } else {
                $state.transitionTo('allfollowups.details', {followupId: nextFollowup.followupId, commentId: nextFollowup.lastReaction.reactionId});
            }
        }


    });
