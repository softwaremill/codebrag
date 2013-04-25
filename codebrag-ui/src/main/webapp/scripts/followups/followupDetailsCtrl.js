angular.module('codebrag.followups')

    .controller('FollowupDetailsCtrl', function ($stateParams, $state, $scope, followupsListService) {

        $scope.markCurrentFollowupAsDone = function() {
            var commitId = $stateParams.id;
            followupsListService.removeFollowupAndGetNext(commitId).then(function(nextFollowup) {
                goTo(nextFollowup);
            });
        };

        function goTo(nextFollowup) {
            if (_.isNull(nextFollowup)) {
                $state.transitionTo('followups.list');
            } else {
                $state.transitionTo('followups.details', {id: nextFollowup.commit.commitId});
            }
        }


    });


