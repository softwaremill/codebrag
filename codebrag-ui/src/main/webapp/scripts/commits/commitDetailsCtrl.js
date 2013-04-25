angular.module('codebrag.commits')

    .controller('CommitDetailsCtrl', function ($stateParams, $state, $scope, filesWithCommentsService, commitsListService, Comments) {

        var commitId = $stateParams.id;
        $scope.generalComments = [];
        $scope.currentCommit = commitsListService.loadCommitById(commitId);
        $scope.files = filesWithCommentsService.loadAll(commitId, $scope.generalComments);

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

        $scope.submitComment = function (content) {
            var comment = {
                commitId: commitId,
                body: content
            };
            Comments.save(comment, function (commentResponse) {
                $scope.generalComments.push(commentResponse.comment);
                $scope.$broadcast('codebrag:commentCreated');
            })
        }

    });


