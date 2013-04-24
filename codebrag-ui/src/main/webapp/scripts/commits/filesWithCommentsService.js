angular.module('codebrag.commits')

    .factory('filesWithCommentsService', function (commitFilesService, Comments) {

        function loadAll(commitId) {
            return commitFilesService.loadFilesForCommit(commitId).$then(function (files) {
                Comments.query({id: commitId}, function (comments) {
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

                function _putCommentsInLine(line) {
                    line.commentCount = 0;
                    if (inlineCommentsForFile != undefined) {
                        var commentLineNumber = undefined;
                        if (line.lineType == "added") {
                            commentLineNumber = line.lineNumberChanged;
                        }
                        else {
                            commentLineNumber = line.lineNumberOriginal;
                        }

                        var commentsForLine = _.find(inlineCommentsForFile.lineComments, function (commentList) {
                            return commentList.lineNumber == commentLineNumber;
                        });

                        if (commentsForLine != undefined) {
                            line.commentCount = commentsForLine.comments.length;
                            file.commentCount += line.commentCount;
                            line.comments = commentsForLine.comments;
                        }
                    }
                    line.hasComments = line.commentCount > 0;
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