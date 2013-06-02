angular.module('codebrag.commits')

    .controller('DiffCtrl', function ($scope, Comments) {

        $scope.userComments = {
            'codebrag-ui-grunt/README.md': {
                4: [
                    {id: '123', authorName: 'Stefan', message: 'test msg'}
                ],
                10: [
                    {id: '123', authorName: 'Stefan', message: 'test msg for 10th line'}
                ]
            }
        };

        $scope.submitInlineComment = function(content, commentData) {
            var newComment = {
                commitId: $scope.currentCommit.commit.id,
                body: content,
                fileName: commentData.fileName,
                lineNumber: commentData.lineNumber
            };

            return Comments.save(newComment).$then(function (commentResponse) {
                var comment = commentResponse.data.comment;

                comment.fileName = newComment.fileName;
                comment.lineNumber = newComment.lineNumber;

                if(_.isUndefined($scope.userComments[comment.fileName][comment.lineNumber])) {
                    $scope.userComments[comment.fileName][comment.lineNumber] = [];
                }
                $scope.userComments[comment.fileName][comment.lineNumber].push(comment);
                $scope.$broadcast('commentAdded', comment);
            });
        };

        $scope.submitComment = function (content) {
            var comment = {
                commitId: $scope.currentCommit.commit.id,
                body: content
            };
            return Comments.save(comment).$then(function (commentResponse) {
                $scope.currentCommit.comments.push(commentResponse.data.comment);
            });
        };

    });


