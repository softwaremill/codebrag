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

        $scope.toggleInlineCommentForm = function(line) {
            line.showCommentForm = (line.commentCount == 0) && !line.showCommentForm;
        };

        $scope.submitInlineComment = function(content, file, line, lineIndex) {
            var comment = {
                commitId: commitId,
                body: content,
                fileName: file.filename,
                lineNumber: lineIndex
            };
            Comments.save(comment, function (commentResponse) {
                line.showCommentForm = false;
                file.commentCount++;
                line.commentCount++;
                line.comments.push(commentResponse.comment);
                $scope.$broadcast('codebrag:commentCreated');
            });
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


