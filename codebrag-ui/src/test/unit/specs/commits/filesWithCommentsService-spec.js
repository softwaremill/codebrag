'use strict';

describe("filesWithCommentsService", function () {

    var rootScope;

    beforeEach(module('codebrag.commits'));

    beforeEach(inject(function ($rootScope) {
        rootScope = $rootScope;
    }));

    it('should leave files untouched if there are no inline comments', inject(function (filesWithCommentsService) {
        // Given
        var files = singleFileDiff("filename.txt");
        var comments = randomCommentsForSingleFile("other-file.txt", [1, 13, 11]);

        // When
        filesWithCommentsService.putInlineCommentsInFiles(files, comments);

        // Then
        expect(files.data[0].commentCount).toBe(0);
        _.forEach(files.data[0].lines, function (line) {
            expect(line.commentCount).toBe(0);
            expect(line.comments.length).toBe(0);
        });
    }));


    it('should count all line comments for each file', inject(function (filesWithCommentsService) {
        // Given

        var files = singleFileDiff("filename.txt");
        var commentsForFile = randomCommentsForSingleFile("filename.txt", [2, 17, 11]);

        // When
        filesWithCommentsService.putInlineCommentsInFiles(files, commentsForFile.inlineComments);

        // Then
        expect(files.data[0].commentCount).toBe(3);
        expect(files.data[0].lines[0].commentCount).toBe(0);
        expect(files.data[0].lines[0].comments.length).toBe(0);

    }));

    it('should add comment data to line', inject(function (filesWithCommentsService) {
        // Given

        var files = singleFileDiff("filename.txt");
        var commentsForFile = randomCommentsForSingleFile("filename.txt", [2, 17]);

        // When
        filesWithCommentsService.putInlineCommentsInFiles(files, commentsForFile.inlineComments);

        // Then
        expect(files.data[0].lines[2].comments[0].message).toBe("message of comment in file filename.txt line 2");
        expect(files.data[0].lines[2].comments[0].authorName).toBe("author of comment in file filename.txt line 2");
        expect(files.data[0].lines[2].comments[0].id).toBe("id of comment in file filename.txt line 2");
        expect(files.data[0].lines[2].comments[0].time).toBe("time of comment in file filename.txt line 2");
    }));

    it('should add comments to proper files', inject(function (filesWithCommentsService) {
        // Given

        var files = singleFileDiff("filename.txt");
        var commentsForFile = randomCommentsForSingleFile("filename.txt", [2, 17]);
        var commentsForAnotherFile = randomCommentsForSingleFile("filename2.txt", [1, 3]);
        joinComments(commentsForFile, commentsForAnotherFile);

        // When
        filesWithCommentsService.putInlineCommentsInFiles(files, commentsForFile.inlineComments);

        // Then
        expect(files.data[0].lines[2].comments.length).toBe(1);
        expect(files.data[0].lines[17].comments.length).toBe(1);
        expect(files.data[0].lines[1].comments.length).toBe(0);
        expect(files.data[0].lines[3].comments.length).toBe(0);
    }));

    it('should add multiple comments to a single line', inject(function (filesWithCommentsService) {
        // Given
        var files = singleFileDiff("Beans.scala");
        var commentsForFile = randomCommentsForSingleFile("Beans.scala", [5]);
        commentsForFile.inlineComments[0].lineComments[0].comments.push(
            {
                authorName: "additional comment author",
                message: "additional comment message",
                id: "additional comment id",
                time: "additional comment time"
            }
        );

        // When
        filesWithCommentsService.putInlineCommentsInFiles(files, commentsForFile.inlineComments);

        // Then
        expect(files.data[0].lines[5].comments.length).toBe(2);
        expect(files.data[0].lines[5].comments[0].authorName).toBe("author of comment in file Beans.scala line 5");
        expect(files.data[0].lines[5].comments[1].authorName).toBe("additional comment author");
    }));

    function joinComments(commentList1, commentList2) {
        commentList1.inlineComments = commentList1.inlineComments.concat(commentList2.inlineComments)
    }

    function singleFileDiff(filename) {
        return {
            data: [
                {"filename": filename, "status": "modified", "lines": [
                    {"line": "diff --git a/codebrag-rest/src/main/scala/com/softwaremill/codebrag/Beans.scala b/codebrag-rest/src/main/scala/com/softwaremill/codebrag/Beans.scala", "lineNumberOriginal": 0, "lineNumberChanged": 0, "lineType": "not-changed"},
                    {"line": "index ec32a4b..71b5d56 100644", "lineNumberOriginal": 1, "lineNumberChanged": 1, "lineType": "not-changed"},
                    {"line": "--- a/codebrag-rest/src/main/scala/com/softwaremill/codebrag/Beans.scala", "lineNumberOriginal": 2, "lineNumberChanged": -1, "lineType": "removed"},
                    {"line": "+++ b/codebrag-rest/src/main/scala/com/softwaremill/codebrag/Beans.scala", "lineNumberOriginal": -1, "lineNumberChanged": 2, "lineType": "added"},
                    {"line": "@@ -11,7 +11,7 @@", "lineNumberOriginal": -1, "lineNumberChanged": -1, "lineType": "header"},
                    {"line": " import service.github._", "lineNumberOriginal": 11, "lineNumberChanged": 11, "lineType": "not-changed"},
                    {"line": " import service.user.Authenticator", "lineNumberOriginal": 12, "lineNumberChanged": 12, "lineType": "not-changed"},
                    {"line": " import pl.softwaremill.common.util.time.RealTimeClock", "lineNumberOriginal": 13, "lineNumberChanged": 13, "lineType": "not-changed"},
                    {"line": "-import com.softwaremill.codebrag.service.github.egit.EgitGitHubCommitImportServiceFactory", "lineNumberOriginal": 14, "lineNumberChanged": -1, "lineType": "removed"},
                    {"line": "+import com.softwaremill.codebrag.service.github.jgit.JgitGitHubCommitImportServiceFactory", "lineNumberOriginal": -1, "lineNumberChanged": 14, "lineType": "added"},
                    {"line": " ", "lineNumberOriginal": 15, "lineNumberChanged": 15, "lineType": "not-changed"},
                    {"line": " ", "lineNumberOriginal": 16, "lineNumberChanged": 16, "lineType": "not-changed"},
                    {"line": " trait Beans {", "lineNumberOriginal": 17, "lineNumberChanged": 17, "lineType": "not-changed"},
                    {"line": "@@ -33,7 +33,7 @@", "lineNumberOriginal": -1, "lineNumberChanged": -1, "lineType": "header"},
                    {"line": "   lazy val converter = new GitHubCommitInfoConverter()", "lineNumberOriginal": 33, "lineNumberChanged": 33, "lineType": "not-changed"},
                    {"line": "   lazy val commitReviewTaskDao = new MongoCommitReviewTaskDAO", "lineNumberOriginal": 34, "lineNumberChanged": 34, "lineType": "not-changed"},
                    {"line": "   lazy val reviewTaskGenerator = new CommitReviewTaskGenerator(userDao, commitReviewTaskDao)", "lineNumberOriginal": 35, "lineNumberChanged": 35, "lineType": "not-changed"},
                    {"line": "-  lazy val importerFactory = new EgitGitHubCommitImportServiceFactory(githubClientProvider, converter, commitInfoDao, reviewTaskGenerator)", "lineNumberOriginal": 36, "lineNumberChanged": -1, "lineType": "removed"},
                    {"line": "+  lazy val importerFactory = new JgitGitHubCommitImportServiceFactory(commitInfoDao, reviewTaskGenerator, userDao)", "lineNumberOriginal": -1, "lineNumberChanged": 36, "lineType": "added"},
                    {"line": "   lazy val followupService = new FollowupService(followupDao, commitInfoDao, commentDao, userDao)", "lineNumberOriginal": 37, "lineNumberChanged": 37, "lineType": "not-changed"},
                    {"line": "   lazy val followupFinder = new MongoFollowupFinder", "lineNumberOriginal": 38, "lineNumberChanged": 38, "lineType": "not-changed"},
                    {"line": "   lazy val commentActivity = new CommentActivity(commentService, followupService)", "lineNumberOriginal": 39, "lineNumberChanged": 39, "lineType": "not-changed"}
                ]}
            ]
        }

    }

    function randomCommentsForSingleFile(filename, linenumbers) {
        var result = { inlineComments: [
            {
                fileName: filename,
                lineComments: []
            }
        ] };

        _.forEach(linenumbers, function (number) {
            var suffix =  " of comment in file " + filename + " line " + number;
            var newCommentList = {
                comments: [
                    {
                        authorName: "author" + suffix,
                        message: "message" + suffix,
                        id: "id" + suffix,
                        time: "time" + suffix
                    }
                ],
                lineNumber: number
            };
            result.inlineComments[0].lineComments.push(newCommentList);
        });
        return result;
    }

});
