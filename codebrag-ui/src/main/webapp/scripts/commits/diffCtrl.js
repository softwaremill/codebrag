angular.module('codebrag.commits')

    .controller('DiffCtrl', function ($scope, Comments) {

        $scope.submitInlineComment = function(content, commentData) {
            var newComment = {
                commitId: $scope.currentCommit.commit.id,
                body: content,
                fileName: commentData.fileName,
                lineNumber: commentData.lineNumber
            };

            return Comments.save(newComment).$then(function (commentResponse) {
                var comment = commentResponse.data.comment;
                addCommentToCommentsCollection(comment, newComment.fileName, newComment.lineNumber);
            });

            function addCommentToCommentsCollection(comment, fileName, lineNumber) {
                var comments = $scope.currentCommit.inlineComments;
                if(_.isUndefined(comments[fileName])) {
                    comments[fileName] = {};
                }
                if(_.isUndefined(comments[fileName][lineNumber])) {
                    comments[fileName][lineNumber] = [];
                }
                comments[fileName][lineNumber].push(comment);
            }
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


