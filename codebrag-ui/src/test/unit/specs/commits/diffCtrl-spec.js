'use strict';

describe("DiffCtrl", function () {

    var $httpBackend;
    var scope;

    beforeEach(module('codebrag.commits'));

    beforeEach(inject(function (_$httpBackend_, $rootScope) {
        $httpBackend = _$httpBackend_;
        scope = $rootScope;
    }));

    afterEach(inject(function (_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));

    it('should call server to submit comment and save this comment locally', inject(function ($controller) {
        // Given
        scope.currentCommit = {
            data: {},
            info: {
                id: 15
            },
            addComment: function (data) {
                this.data = data;
            }
        };
        var returnedCommitData = {comment: 'data'};
        $controller('DiffCtrl', {$scope: scope});
        $httpBackend.whenPOST('rest/commits/15/comments', '{"commitId":15,"body":"comment content"}').respond(201, returnedCommitData);

        // When
        scope.submitComment('comment content');
        $httpBackend.flush();

        // Then
        expect(scope.currentCommit.data).toEqual('data')
    }));

    it('should call server to submit inline comment and save this comment locally', inject(function ($controller) {
        // Given
        scope.currentCommit = {
            data: {},
            info: {
                id: 15
            },
            addInlineComment: function (comment, fileName, lineNumber) {
                this.data.comment = comment;
                this.data.fileName = fileName;
                this.data.lineNumber = lineNumber;
            }
        };
        var returnedCommitData = {comment: 'data'};
        $controller('DiffCtrl', {$scope: scope});
        $httpBackend.expectPOST('rest/commits/15/comments',
            '{"commitId":15,"body":"comment content","fileName":"file","lineNumber":3}').respond(201, returnedCommitData);
        var inputCommentData = {fileName: 'file', lineNumber: 3};

        // When
        scope.submitInlineComment('comment content', inputCommentData);
        $httpBackend.flush();

        // Then
        expect(scope.currentCommit.data.comment).toEqual('data');
        expect(scope.currentCommit.data.fileName).toEqual('file');
        expect(scope.currentCommit.data.lineNumber).toEqual(3);
    }));

    it('should call server to submit inline like and save this like locally', inject(function ($controller, authService) {
        // Given
        authService.loggedInUser = {fullName: 'irrelevantName'};
        scope.currentCommit = {
            data: {},
            info: {
                id: 15
            },
            addLike: function (like, fileName, lineNumber) {
                this.data.like = like;
                this.data.fileName = fileName;
                this.data.lineNumber = lineNumber;
            },
            isUserAuthorOfCommit: function () {
                return false;
            },
            userAlreadyLikedLine: function () {
                return false;
            }

        };
        var returnedLikeData = {likeData: 'data'};
        var likeLineNumber = 13;
        var likeFileName = 'filename';
        $controller('DiffCtrl', {$scope: scope});
        $httpBackend.expectPOST('rest/commits/15/likes',
            '{"commitId":15,"fileName":"filename","lineNumber":13}').respond(201, returnedLikeData);

        // When
        scope.like(likeFileName, likeLineNumber);
        $httpBackend.flush();

        // Then
        expect(scope.currentCommit.data.like).toEqual(returnedLikeData);
        expect(scope.currentCommit.data.fileName).toEqual(likeFileName);
        expect(scope.currentCommit.data.lineNumber).toEqual(likeLineNumber);
    }));

    it('should not send anything when user is author of the commit', inject(function ($controller, authService) {
        // Given
        var userName = 'user name';
        authService.loggedInUser = {fullName: userName};
        scope.currentCommit = {
            data: {},
            info: {
                id: 15
            },
            isUserAuthorOfCommit: function (name) {
                return name == userName;
            },
            userAlreadyLikedLine: function () {
                return false;
            }
        };
        var likeLineNumber = 13;
        var likeFileName = 'filename';
        $controller('DiffCtrl', {$scope: scope});

        // When
        scope.like(likeFileName, likeLineNumber);

        // Then server not called
    }));

    it('should not allow posting more than one like for a line by same user', inject(function ($controller, authService) {
        // Given
        var userName = 'user name';
        authService.loggedInUser = {fullName: userName};
        scope.currentCommit = {
            data: {},
            info: {
                id: 15
            },
            isUserAuthorOfCommit: function () {
                return false;
            },
            userAlreadyLikedLine: function (name) {
                return userName == name;
            }
        };
        var likeLineNumber = 13;
        var likeFileName = 'filename';
        $controller('DiffCtrl', {$scope: scope});

        // When
        scope.like(likeFileName, likeLineNumber);

        // Then server not called
    }));

});
