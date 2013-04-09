'use strict';

describe("Comment Controller", function () {

    beforeEach(module('codebrag.commits'));

    var scope, $httpBackend, selectedCommit;
    var commentsEndpointAddress = 'rest/commits/1/comments';
    var singleStoredComment = {id: 123, authorName: "mostr", message: "this is comment"};

    beforeEach(inject(function (_$httpBackend_, $rootScope) {
        $httpBackend = _$httpBackend_;
        scope = $rootScope.$new();
        selectedCommit = {id: 1};
    }));

    afterEach(inject(function (_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));

    it('should post a new comment to the server', inject(function ($controller, $stateParams) {
        // Given
        var addCommand = {commitId: selectedCommit.id, body: "new message"};
        var serverResponseComment = {"id": "1", "authorName": "author", "message": "ok", "time": "2013-03-29T15:14:10Z"}
        givenStoredSingleComment();
        $stateParams.id = selectedCommit.id;
        $httpBackend.expectPOST(commentsEndpointAddress, addCommand).respond({comment: serverResponseComment});

        // When
        $controller('CommentCtrl', {$scope: scope});
        scope.addComment.body = "new message";
        scope.submitComment();
        $httpBackend.flush();

        // Then
        expect(scope.commentsList[0]).toEqual(singleStoredComment);
        expect(scope.commentsList[1]).toEqual(serverResponseComment);
    }));

    it('should reset comment form to empty after saving comment', inject(function($controller) {
        // Given
        givenStoredSingleComment();
        $httpBackend.expectPOST(commentsEndpointAddress, scope.addComment).respond('');
        $controller('CommentCtrl', {$scope: scope});

        // When
        scope.addComment.commitId = selectedCommit.id;
        scope.addComment.body = 'this is comment';
        scope.submitComment();
        $httpBackend.flush();

        // Then
        expect(scope.addComment.body).toEqual('');

    }));

    it('should load all comments for selected commit on start', inject(function ($controller) {
        // Given
        givenStoredSingleComment()

        // When
        $controller('CommentCtrl', {$scope: scope, currentCommit: selectedCommit});
        $httpBackend.flush();

        // Then
        expect(scope.commentsList).toEqual([singleStoredComment]);
    }));

    function givenStoredSingleComment() {
        var commentList = {comments: [singleStoredComment]};
        $httpBackend.whenGET(commentsEndpointAddress).respond(commentList);
    }

})




