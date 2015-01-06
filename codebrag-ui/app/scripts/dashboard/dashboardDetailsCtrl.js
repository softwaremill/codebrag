angular.module('codebrag.dashboard')

    .controller('DashboardDetailsCtrl', function ($stateParams, $state, $scope, allFollowupsService, commitsService) {

        var followupId = $stateParams.followupId;

        $scope.scrollTo = $stateParams.commentId;

        allFollowupsService.loadFollowupDetails(followupId).then(function(followup) {
            $scope.currentFollowup = followup;
            commitsService.commitDetails(followup.commit.sha, followup.commit.repoName).then(function(commit) {
                $scope.currentCommit = new codebrag.CurrentCommit(commit);

            });
        });

        $scope.markCurrentFollowupAsDone = function() {
            allFollowupsService.removeAndGetNext(followupId).then(function(nextFollowup) {
                goTo(nextFollowup);
            });
        };

        function goTo(nextFollowup) {
            if (_.isNull(nextFollowup)) {
                $state.transitionTo('dashboard.list');
            } else {
                $state.transitionTo('dashboard.details', {followupId: nextFollowup.followupId, commentId: nextFollowup.lastReaction.reactionId});
            }
        }


    });
