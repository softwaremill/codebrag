'use strict';

describe("CommitDetailsController", function () {

    var $httpBackend;
    var selectedCommitId = 123;
    var noopPromise = {then: function(){}};

    beforeEach(module('codebrag.commits'));

    beforeEach(inject(function (_$httpBackend_) {
        $httpBackend = _$httpBackend_;
    }));

    afterEach(inject(function (_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));

    it('should use commit ID provided in $stateParams to load commit data', inject(function ($controller, $stateParams) {
        // Given
        var scope = {};
        $stateParams.id = selectedCommitId;
        $httpBackend.expectGET(commitDetailsFor(selectedCommitId)).respond();
        $httpBackend.expectGET(commitFilesFor(selectedCommitId)).respond();
        $httpBackend.expectGET(commitCommentsFor(selectedCommitId)).respond();

        // When
        $controller('CommitDetailsCtrl', {$scope:scope});

        // Then
        $httpBackend.flush();
    }));

    it('should load files and details for selected commit', inject(function ($controller, $stateParams, filesWithCommentsService, commitsListService) {
        // Given
        var scope = {};
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

    it('should call service to mark current commit as reviewed', inject(function($controller, $stateParams, commitsListService) {
        // Given
        var scope = {};
        $stateParams.id = selectedCommitId;
        $httpBackend.expectGET(commitDetailsFor(selectedCommitId)).respond();
        $httpBackend.expectGET(commitFilesFor(selectedCommitId)).respond();
        $httpBackend.expectGET(commitCommentsFor(selectedCommitId)).respond();
        spyOn(commitsListService, 'removeCommitAndGetNext').andReturn(noopPromise);

        // When
        $controller('CommitDetailsCtrl', {$scope:scope});
        scope.markCurrentCommitAsReviewed();
        $httpBackend.flush();

        // Then
        expect(commitsListService.removeCommitAndGetNext).toHaveBeenCalled();
    }));

    it('should call service to mark current commit as reviewed', inject(function($controller, $stateParams, commitsListService) {
        // Given
        var scope = {};
        $stateParams.id = selectedCommitId;
        $httpBackend.expectGET(commitDetailsFor(selectedCommitId)).respond();
        $httpBackend.expectGET(commitFilesFor(selectedCommitId)).respond();
        $httpBackend.expectGET(commitCommentsFor(selectedCommitId)).respond();
        spyOn(commitsListService, 'removeCommitAndGetNext').andReturn(noopPromise);

        // When
        $controller('CommitDetailsCtrl', {$scope:scope});
        scope.markCurrentCommitAsReviewed();
        $httpBackend.flush();

        // Then
        expect(commitsListService.removeCommitAndGetNext).toHaveBeenCalled();

    }));

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
