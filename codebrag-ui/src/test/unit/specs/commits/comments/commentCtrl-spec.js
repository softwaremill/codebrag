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
        var addCommand = {commitId: selectedCommit.id, body: "new message"};
        var serverResponseComment = {"id":"1","authorName":"author","message":"ok","time":"2013-03-29T15:14:10Z"}
        var alreadyPresentComment = {"id":"0","authorName":"robert","message":":)","time":"2013-02-29T15:14:10Z"}
        $httpBackend.whenGET(commentsEndpointAddress).respond({comments:[alreadyPresentComment]});
        $httpBackend.expectPOST(commentsEndpointAddress, addCommand).respond({item: serverResponseComment});
        $controller('CommentCtrl', {$scope: scope, currentCommit: selectedCommit});

        // When
        scope.addComment.body = "new message"
        scope.submitComment()
        $httpBackend.flush();

        // Then
        expect(scope.commentsList[0]).toEqual(alreadyPresentComment)
        expect(scope.commentsList[1]).toEqual(serverResponseComment)
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




