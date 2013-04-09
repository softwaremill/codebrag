'use strict';

describe("CommitDetailsController", function () {

    var $httpBackend;
    var selectedCommitId = 123;

    beforeEach(module('codebrag.commits'));

    beforeEach(inject(function (_$httpBackend_) {
        $httpBackend = _$httpBackend_;
    }));

    afterEach(inject(function (_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));

    it('should use commit ID provided in $stateParams to load commit details', inject(function ($controller, $stateParams) {
        // Given
        var scope = {};
        $stateParams.id = selectedCommitId;
        $httpBackend.whenGET(commitDetailsFor(selectedCommitId)).respond('[{"filename":"test.txt", "lines":[]}]');

        // When
        $controller('CommitDetailsCtrl', {$scope:scope});
        $httpBackend.flush();

        // Then
        expect(scope.commitId).toEqual($stateParams.id);
    }));

    it('should load files for selected commit', inject(function ($controller, $stateParams) {
        // Given
        var scope = {};
        $stateParams.id = selectedCommitId;
        $httpBackend.whenGET(commitDetailsFor(selectedCommitId)).respond('[{"filename":"test.txt", "lines":[]}]');

        // When
        $controller('CommitDetailsCtrl', {$scope:scope});
        $httpBackend.flush();

        //then
        expect(scope.files).not.toBeNull();
    }));

    function commitDetailsFor(id) {
        return 'rest/commits/' + id;
    }

});
