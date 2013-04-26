angular.module('codebrag.commits')

    .factory('filesWithCommentsService', function (commitFilesService, Comments) {

        function loadAll(commitId, generalComments) {
            return commitFilesService.loadFilesForCommit(commitId).$then(function (files) {
                Comments.query({id: commitId}, function (comments) {
                    _.forEach(comments.comments, function(comment) {generalComments.push(comment)});
                    putInlineCommentsInFiles(files, comments.inlineComments);
                });
                return files.data;
            });
        }

        function putInlineCommentsInFiles(files, allComments) {

            _.forEach(files.data, _putCommentsInFile);

            function _putCommentsInFile(file) {
                var inlineCommentsForFile = _.find(allComments, _isCommentListForFile);
                file.commentCount = 0;

                _.forEach(file.lines, _putCommentsInLine);

                function _putCommentsInLine(line, index) {
                    line.commentCount = 0;
                    line.comments = [];
                    if (!_.isUndefined(inlineCommentsForFile)) {
                        var commentsForLine = _.find(inlineCommentsForFile.lineComments, function (commentList) {
                            return commentList.lineNumber == index;
                        });
                        if (!_.isUndefined(commentsForLine)) {
                            line.commentCount = commentsForLine.comments.length;
                            file.commentCount += line.commentCount;
                            line.comments = commentsForLine.comments;
                        }
                    }
                    line.hasComments = line.commentCount > 0;
                    line.showCommentForm = false;
                }

                function _isCommentListForFile(commentList) {
                    return commentList.fileName == file.filename;
                }

            }
        }

        return {
            loadAll: loadAll,
            putInlineCommentsInFiles: putInlineCommentsInFiles
        }

    });