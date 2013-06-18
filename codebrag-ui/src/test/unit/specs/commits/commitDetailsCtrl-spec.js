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

    function promiseResolvedWith(value) {
        return {
            then: function(successHandler) {
                successHandler(value);
            }
        }
    }

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

    it('should load diff for selected commit', inject(function ($controller, $stateParams, commitsListService, $q) {
        // Given
        $stateParams.id = selectedCommitId;
        var expectedCommitDetails = {commit: {sha: '123'}, diff: [], supressedFiles: []};
        spyOn(commitsListService, 'loadCommitById').andReturn(promiseResolvedWith(expectedCommitDetails));

        // When
        $controller('CommitDetailsCtrl', {$scope:scope});

        //then
        expect(scope.currentCommit.info).toBe(expectedCommitDetails.commit);
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

});
