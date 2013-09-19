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
        expect(scope.currentCommit.data).toEqual('data');
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
        var currentUser = authService.loggedInUser = {id: '123', fullName: 'Bob Smith', email: 'bob@smith.com'};
        var commit = {
            commit: {
                id: '123',
                authorName: 'John Doe',
                authorEmail: 'john@doe.com'
            }
        };
        scope.currentCommit = new codebrag.CurrentCommit(commit);
        var likeData = {
            commitId: commit.commit.id,
            fileName: 'file.txt',
            lineNumber: 13
        };
        $controller('DiffCtrl', {$scope: scope});
        var serverReturnedLikeData = angular.copy(likeData);
        angular.extend(serverReturnedLikeData, {id: '456', authorId: currentUser.id});
        $httpBackend.expectPOST('rest/commits/123/likes', likeData).respond(200, serverReturnedLikeData);

        // When
        scope.like(likeData.fileName, likeData.lineNumber);
        $httpBackend.flush();

        // Then
        var savedLike = scope.currentCommit.findLikeFor(currentUser, likeData.fileName, likeData.lineNumber);
        expect(savedLike).toEqual(serverReturnedLikeData);
    }));

    it('should not send anything when user is author of the commit', inject(function ($controller, authService) {
        // Given
        authService.loggedInUser = {id: '123', fullName: 'John Doe', email: 'john@doe.com'};
        var commit = {
            commit: {
                id: '123',
                authorName: 'John Doe',
                authorEmail: 'john@doe.com'
            }
        };
        scope.currentCommit = new codebrag.CurrentCommit(commit);
        $controller('DiffCtrl', {$scope: scope});

        // When
        scope.like('test.txt', 123);

        // Then server not called
    }));

    it('should not allow posting more than one like for a line by same user', inject(function ($controller, authService) {
        // Given
        authService.loggedInUser = {id: '123', fullName: 'John Doe', email: 'john@doe.com'};
        var commitId = '456';
        var fileName = 'test.txt';
        var lineNumber = 33;
        var commit = {
            commit: {
                id: commitId,
                authorName: 'Bob Smith',
                authorEmail: 'bob@smith.com'
            },
            lineReactions: {
                'test.txt': {
                    33: {
                        likes: [
                            {
                                commitId: commitId,
                                fileName: fileName,
                                lineNumber: lineNumber,
                                authorId: authService.loggedInUser.id,
                                authorEmail: authService.loggedInUser.email
                            }
                        ]
                    }
                }
            }
        };
        scope.currentCommit = new codebrag.CurrentCommit(commit);
        $controller('DiffCtrl', {$scope: scope});

        // When
        scope.like(fileName, lineNumber);

        // Then server not called
    }));

});
