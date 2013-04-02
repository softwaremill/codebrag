'use strict';

describe("Comment Controller", function () {

    beforeEach(module('codebrag.commits'));

    var scope, $httpBackend, selectedCommit;
    var commentsEndpointAddress = 'rest/commits/1/comments';
    var singleStoredComment = {id: 123, authorName: "mostr", message: "this is comment"};

    beforeEach(inject(function (_$httpBackend_, $rootScope, currentCommit) {
        $httpBackend = _$httpBackend_;
        scope = $rootScope.$new();
        selectedCommit = currentCommit;
        selectedCommit.id = 1;
    }));

    afterEach(inject(function (_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));

    it('should post a new comment to the server', inject(function ($controller) {
        // Given
        var addCommand = {commitId: selectedCommit.id, body: "new message"};
        var serverResponseComment = {"id": "1", "authorName": "author", "message": "ok", "time": "2013-03-29T15:14:10Z"}
        givenStoredSingleComment();
        $httpBackend.expectPOST(commentsEndpointAddress, addCommand).respond({comment: serverResponseComment});

        // When
        $controller('CommentCtrl', {$scope: scope, currentCommit: selectedCommit});
        scope.addComment.body = "new message"
        scope.submitComment()
        $httpBackend.flush();

        // Then
        expect(scope.commentsList[0]).toEqual(singleStoredComment)
        expect(scope.commentsList[1]).toEqual(serverResponseComment)
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




