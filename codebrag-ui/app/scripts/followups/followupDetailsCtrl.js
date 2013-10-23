angular.module('codebrag.followups')

    .controller('FollowupDetailsCtrl', function ($stateParams, $state, $scope, followupsService, commitsService) {

        var followupId = $stateParams.followupId;

        $scope.scrollTo = $stateParams.commentId;

        followupsService.loadFollowupDetails(followupId).then(function(followup) {
            $scope.currentFollowup = followup;
            commitsService.commitDetails(followup.commit.commitId).then(function(commit) {
                $scope.currentCommit = new codebrag.CurrentCommit(commit);

            });
        });

        $scope.markCurrentFollowupAsDone = function() {
            followupsService.removeAndGetNext(followupId).then(function(nextFollowup) {
                goTo(nextFollowup);
            });
        };

        function goTo(nextFollowup) {
            if (_.isNull(nextFollowup)) {
                $state.transitionTo('followups.list');
            } else {
                $state.transitionTo('followups.details', {followupId: nextFollowup.followupId, commentId: nextFollowup.lastReaction.reactionId});
            }
        }


    });