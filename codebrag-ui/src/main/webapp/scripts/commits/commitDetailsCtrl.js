angular.module('codebrag.commits')

    .controller('CommitDetailsCtrl', function ($stateParams, $state, $scope, commitFilesService, commitsListService) {

        var commitId = $stateParams.id;

        $scope.currentCommit = commitsListService.loadCommitById(commitId);
        $scope.files = commitFilesService.loadFilesForCommit(commitId);

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


