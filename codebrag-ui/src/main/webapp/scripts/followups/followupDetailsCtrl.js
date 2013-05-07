angular.module('codebrag.followups')

    .controller('FollowupDetailsCtrl', function ($stateParams, $state, $scope, followupsListService, commitsListService) {

        var followupId = $stateParams.id;

        followupsListService.loadFollowupById(followupId).then(function(followup) {
            $scope.currentFollowup = followup;
            commitsListService.loadCommitById(followup.commit.commitId).then(function(commit) {
                $scope.currentCommit = commit;
            })
        });

        $scope.markCurrentFollowupAsDone = function() {
            followupsListService.removeFollowupAndGetNext(followupId).then(function(nextFollowup) {
                goTo(nextFollowup);
            });
        };

        function goTo(nextFollowup) {
            if (_.isNull(nextFollowup)) {
                $state.transitionTo('followups.list');
            } else {
                $state.transitionTo('followups.details', {id: nextFollowup.followupId});
            }
        }


    });