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

    afterEach(inject(function(_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));

    it('should post a new comment to the server', inject(function ($controller) {
        // Given
        var expectedSavePayload = {commitId: selectedCommit.id, body: "new message"};
        $httpBackend.whenGET(commentsEndpointAddress).respond({comments:[]});
        $httpBackend.expectPOST(commentsEndpointAddress, expectedSavePayload).respond({});
        $controller('CommentCtrl', {$scope: scope, currentCommit: selectedCommit});

        // When
        scope.addComment.body = "new message"
        scope.submitComment()
        $httpBackend.flush();

        //Then
        // server was called as expected
    }));

    it('should load all comments for selected commit on start', inject(function($controller) {
        // Given
        var commentsList = {comments: [singleStoredComment]};
        $httpBackend.whenGET(commentsEndpointAddress).respond(commentsList);

        // When
        $controller('CommentCtrl', {$scope: scope, currentCommit: selectedCommit});
        $httpBackend.flush();

        // Then
        expect(scope.commentsList).toEqual(commentsList.comments);
    }));
})




