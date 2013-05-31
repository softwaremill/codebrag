angular.module('codebrag.commits')

    .controller('DiffCtrl', function ($scope, Comments) {

        $scope.submitInlineComment = function(content, commentData) {
            var comment = {
                commitId: $scope.currentCommit.commit.id,
                body: content,
                fileName: commentData.fileName,
                lineNumber: commentData.lineNumber
            };
            console.log(comment);
//            return Comments.save(comment).$then(function (commentResponse) {
//                line.comments.push(commentResponse.data.comment);
//            });
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


