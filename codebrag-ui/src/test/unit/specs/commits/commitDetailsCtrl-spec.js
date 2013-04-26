'use strict';

describe("CommitDetailsController", function () {

    var $httpBackend;
    var selectedCommitId = 123;
    var noopPromise = {then: function(){}};
    var selectedCommit, scope;
    var commentsEndpointAddress = 'rest/commits/123/comments';
    beforeEach(module('codebrag.commits'));
    var singleStoredComment = {id: '123', authorName: "mostr", message: "this is comment", time: "2013-03-29T15:14:10Z"};

    beforeEach(inject(function (_$httpBackend_, $rootScope) {
        $httpBackend = _$httpBackend_;
        scope = $rootScope.$new();
        selectedCommit = {id: 1};
    }));

    afterEach(inject(function (_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));

    it('should use commit ID provided in $stateParams to load commit data', inject(
        function ($controller, $stateParams, commitsListService, filesWithCommentsService) {

        // Given
        $stateParams.id = selectedCommitId;
        spyOn(filesWithCommentsService, "loadAll");
        spyOn(commitsListService, "loadCommitById");

        // When
        $controller('CommitDetailsCtrl', {$scope:scope});

        // Then
        expect(commitsListService.loadCommitById).toHaveBeenCalledWith(selectedCommitId);
    }));

    it('should load files and details for selected commit', inject(function ($controller, $stateParams, filesWithCommentsService, commitsListService) {
        // Given
        $stateParams.id = selectedCommitId;
        var expectedCommitDetails = {id: selectedCommitId, sha: '123'};
        var expectedCommitFiles = [{filename: 'file1.txt', lines: []}];
        spyOn(commitsListService, 'loadCommitById').andReturn(expectedCommitDetails);
        spyOn(filesWithCommentsService, 'loadAll').andReturn(expectedCommitFiles);

        // When
        $controller('CommitDetailsCtrl', {$scope:scope});

        //then
        expect(scope.files.length).toBe(expectedCommitFiles.length);
        expect(scope.currentCommit.id).toBe(expectedCommitDetails.id);
        expect(scope.currentCommit.sha).toBe(expectedCommitDetails.sha);
    }));
//
    it('should call service to mark current commit as reviewed', inject( function
        ($controller, $stateParams, commitsListService, filesWithCommentsService) {
        // Given
        $stateParams.id = selectedCommitId;
        spyOn(commitsListService, 'loadCommitById');
        spyOn(filesWithCommentsService, 'loadAll');
        spyOn(commitsListService, 'removeCommitAndGetNext').andReturn(noopPromise);

        // When
        $controller('CommitDetailsCtrl', {$scope:scope});
        scope.markCurrentCommitAsReviewed();

        // Then
        expect(commitsListService.removeCommitAndGetNext).toHaveBeenCalledWith(selectedCommitId);
    }));

    it('should load all files and comments for selected commit on start', inject(function
        ($controller, $stateParams, commitsListService, filesWithCommentsService) {
        // Given
        $stateParams.id = selectedCommitId;
        spyOn(commitsListService, 'loadCommitById');
        givenStoredSingleComment(filesWithCommentsService);

        // When
        $controller('CommitDetailsCtrl', {$scope: scope});

        // Then
        expect(scope.generalComments[0]).toEqual(singleStoredComment);
    }));

    it('should add general comment after posting to server', inject(function
        ($controller, $stateParams, commitsListService, filesWithCommentsService) {
        // Given
        $stateParams.id = selectedCommitId;
        var addComment = {commitId: selectedCommitId, body: "added comment"};
        var serverResponseComment = {"id": "1", "authorName": "author", "message": addComment.body, "time": "2013-03-29T15:14:10Z"};
        spyOn(commitsListService, 'loadCommitById');
        givenStoredSingleComment(filesWithCommentsService);
        $httpBackend.expectPOST(commentsEndpointAddress, addComment).respond({comment: serverResponseComment});

        // When
        $controller('CommitDetailsCtrl', {$scope: scope});
        scope.submitComment(addComment.body);
        $httpBackend.flush();

        // Then
        expect(scope.generalComments[0]).toEqual(singleStoredComment);
        expect(scope.generalComments[1]).toEqual(serverResponseComment);
    }));

    it('should fire event when comment added', inject(function
        ($controller, $stateParams, commitsListService, filesWithCommentsService) {
        // Given
        $stateParams.id = selectedCommitId;
        givenStoredSingleComment(filesWithCommentsService);
        var addComment = {commitId: selectedCommitId, body: "ok"};
        var serverResponseComment = {"id": "1", "authorName": "author", "message": addComment.body, "time": "2013-03-29T15:14:10Z"};
        spyOn(commitsListService, 'loadCommitById');
        $httpBackend.expectPOST(commentsEndpointAddress, addComment).respond({comment: serverResponseComment});
        spyOn(scope, '$broadcast');

        // When
        $controller('CommitDetailsCtrl', {$scope: scope});
        scope.submitComment(addComment.body);
        $httpBackend.flush();

        // Then
        expect(scope.$broadcast).toHaveBeenCalledWith('codebrag:commentCreated');
    }));

    it('should add a new inline comment after posting to server', inject(function
        ($controller, $stateParams, commitsListService, filesWithCommentsService) {
        // Given
        $stateParams.id = selectedCommitId;
        var addComment = {commitId: selectedCommitId, body: "new inline comment body", lineNumber: 5};
        var serverResponseComment = {"id": "1", "authorName": "author", "message": addComment.body, "time": "2013-03-29T15:14:10Z"};
        spyOn(commitsListService, 'loadCommitById');
        givenStoredSingleComment(filesWithCommentsService);
        $httpBackend.expectPOST(commentsEndpointAddress, addComment).respond({comment: serverResponseComment});
        var file = {
            commentCount: 0
        };

        var line = {
            showCommentForm: true,
            commentCount: 0,
            comments: []
        };

        // When
        $controller('CommitDetailsCtrl', {$scope: scope});
        scope.submitInlineComment(addComment.body, file, line, 5);
        $httpBackend.flush();

        // Then
        expect(file.commentCount).toEqual(1);
        expect(line.commentCount).toEqual(1);
        expect(line.showCommentForm).toBeFalsy();
        expect(line.comments[0]).toEqual(serverResponseComment);
    }));

    function givenStoredSingleComment(filesWithCommentsService) {
        spyOn(filesWithCommentsService, "loadAll").andCallFake(
            function () {
                scope.generalComments.push(singleStoredComment)
            }
        );
    }

    function commitDetailsFor(id) {
        return 'rest/commits/' + id;
    }

    function commitCommentsFor(id) {
        return 'rest/commits/' + id + '/comments';
    }

    function commitFilesFor(id) {
        return 'rest/commits/' + id + '/files';
    }

});
