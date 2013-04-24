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
            expect(line.comments).toBeUndefined();
        });
    }));


    it('should count all line comments for each file', inject(function (filesWithCommentsService) {
        // Given

        var files = singleFileDiff("filename.txt");
        var commentsForFile = randomCommentsForSingleFile("filename.txt", [1, 13, 11]);

        // When
        filesWithCommentsService.putInlineCommentsInFiles(files, commentsForFile.inlineComments);

        // Then
        expect(files.data[0].commentCount).toBe(3);
        expect(files.data[0].lines[0].commentCount).toBe(0);
        expect(files.data[0].lines[0].comments).toBeUndefined();

    }));

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
            var newCommentList = {
                comments: [
                    {
                        authorName: "Sofokles",
                        message: "comment message",
                        id: "comment id",
                        time: "2013-04-01T06:39:12Z"
                    }
                ],
                lineNumber: number
            };
            result.inlineComments[0].lineComments.push(newCommentList);
        });
        return result;
    }

});
