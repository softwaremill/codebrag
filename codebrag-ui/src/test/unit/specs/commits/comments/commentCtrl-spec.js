'use strict';

describe("Comment Controller", function () {

    beforeEach(module('codebrag.commits'));

    var scope, $httpBackend, ctrl, commentService;

    beforeEach(inject(function (_$httpBackend_, $rootScope, $controller, Comments) {
        $httpBackend = _$httpBackend_;
        commentService = Comments;
        scope = $rootScope.$new();
        ctrl = $controller
    }));

    it('should post a new comment to the server', function () {
        // Given
        $httpBackend.whenPOST('rest/commits/1/comments').respond({id: 'new-comment-id'});

        // When
        ctrl('CommentCtrl', {$scope: scope, currentCommit: {id: '1'}});
        scope.addComment.body = "new message"
        scope.submitComment()
        $httpBackend.flush();

        //Then
        // server was called as expected
    })
})




