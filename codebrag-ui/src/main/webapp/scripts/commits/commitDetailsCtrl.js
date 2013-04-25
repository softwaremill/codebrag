angular.module('codebrag.commits')

    .controller('CommitDetailsCtrl', function ($stateParams, $state, $scope, filesWithCommentsService, commitsListService) {

        var commitId = $stateParams.id;

        $scope.currentCommit = commitsListService.loadCommitById(commitId);
        $scope.files = filesWithCommentsService.loadAll(commitId);

        $scope.markCurrentCommitAsReviewed = function() {
            commitsListService.removeCommitAndGetNext(commitId).then(function(nextCommit) {
                goTo(nextCommit);
            })
        };

        $scope.submitInlineComment = function(content, file, line, lineIndex) {
            // TODO
            var comment = {
                commitId: commitId,
                body: content
            };
            console.log("submitting comment for " + file.filename + "; line " + lineIndex)
        };

        function goTo(nextCommit) {
            if (_.isNull(nextCommit)) {
                $state.transitionTo('commits.list');
            } else {
                $state.transitionTo('commits.details', {id: nextCommit.id});
            }
        }

    });


