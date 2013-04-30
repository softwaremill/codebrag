'use strict';

describe("CommitDetailsController", function () {

    var $httpBackend;
    var q;
    var selectedCommitId = 123;
    var noopPromise = {then: function(){}};
    var selectedCommit, scope;
    var commentsEndpointAddress = 'rest/commits/123/comments';
    beforeEach(module('codebrag.commits'));
    var singleStoredComment = {id: '123', authorName: "mostr", message: "this is comment", time: "2013-03-29T15:14:10Z"};

    beforeEach(inject(function (_$httpBackend_, $rootScope, $q) {
        $httpBackend = _$httpBackend_;
        scope = $rootScope.$new();
        selectedCommit = {id: 1};
        q = $q;
    }));

    afterEach(inject(function (_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));

    it('should use commit ID provided in $stateParams to load commit data', inject(
        function ($controller, $stateParams, commitsListService) {

        // Given
        $stateParams.id = selectedCommitId;

        spyOn(commitsListService, "loadCommitById").andReturn(noopPromise);

        // When
        $controller('CommitDetailsCtrl', {$scope:scope});

        // Then
        expect(commitsListService.loadCommitById).toHaveBeenCalledWith(selectedCommitId);
    }));

    it('should load files and details for selected commit', inject(function ($controller, $stateParams, commitsListService) {
        // Given
        $stateParams.id = selectedCommitId;
        var expectedCommitDetails = {commit: {sha: '123'}, comments: [], files: []};
        var deferred = q.defer();
        deferred.resolve(expectedCommitDetails);
        spyOn(commitsListService, 'loadCommitById').andReturn(deferred.promise);

        // When
        $controller('CommitDetailsCtrl', {$scope:scope});
        scope.$apply();

        //then
        expect(scope.currentCommit.commit).toBe(expectedCommitDetails.commit);
        expect(scope.currentCommit.comments).toBe(expectedCommitDetails.comments);
        expect(scope.currentCommit.files).toBe(expectedCommitDetails.files);
    }));
//
    it('should call service to mark current commit as reviewed', inject( function
        ($controller, $stateParams, commitsListService) {
        // Given
        $stateParams.id = selectedCommitId;
        spyOn(commitsListService, 'loadCommitById').andReturn(noopPromise);
        spyOn(commitsListService, 'removeCommitAndGetNext').andReturn(noopPromise);

        // When
        $controller('CommitDetailsCtrl', {$scope:scope});
        scope.markCurrentCommitAsReviewed();

        // Then
        expect(commitsListService.removeCommitAndGetNext).toHaveBeenCalledWith(selectedCommitId);
    }));

    it('should add comment to list after posting to server', inject(function
        ($controller, $stateParams, commitsListService) {
        // Given
        $stateParams.id = selectedCommitId;
        var addComment = {commitId: selectedCommitId, body: "added comment"};
        var serverResponseComment = {"id": "1", "authorName": "author", "message": addComment.body, "time": "2013-03-29T15:14:10Z"};
        spyOn(commitsListService, 'loadCommitById').andReturn(noopPromise);
        $httpBackend.expectPOST(commentsEndpointAddress, addComment).respond({comment: serverResponseComment});
        scope.currentCommit = {commit: {sha: '123'}, comments: []};

        // When
        $controller('CommitDetailsCtrl', {$scope: scope});
        scope.submitComment(addComment.body);
        $httpBackend.flush();

        // Then
        expect(scope.currentCommit.comments.length).toBe(1);
    }));

});
