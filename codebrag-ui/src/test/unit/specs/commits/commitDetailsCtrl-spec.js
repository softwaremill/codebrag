'use strict';

describe("CommitDetailsController", function () {

    var selectedCommit = {id: 123, sha: '123abc123'};
    var $httpBackend;

    beforeEach(module('codebrag.commits'));

    beforeEach(inject(function (_$httpBackend_) {
        $httpBackend = _$httpBackend_;
    }));

    afterEach(inject(function (_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));

    it('should receive selected commit info from commits list element', inject(function ($controller) {
        // Given
        var commitsListItemScope = {};
        var commitDetailsScope = {};
        $controller('CommitsListItemCtrl', {$scope: commitsListItemScope});
        $controller('CommitDetailsCtrl', {$scope: commitDetailsScope});

        // When
        commitsListItemScope.openCommitDetails(selectedCommit);

        // Then
        expect(commitDetailsScope.currentCommit.id).toEqual(selectedCommit.id);
        expect(commitDetailsScope.currentCommit.sha).toEqual(selectedCommit.sha);
    }));

    it('should load files for selected commit', inject(function ($controller, currentCommit) {
        //given
        currentCommit.id = 1;

        var currentScope = {};

        $httpBackend.whenGET('rest/commits/1').respond('[{"filename":"test.txt", "lines":[]}]');

        //when
        $controller('CommitDetailsCtrl', {$scope: currentScope});
        $httpBackend.flush();

        //then
        expect(currentScope.files).not.toBeNull();
    }));

    it('should not attempt to load files if no commit is selected', inject(function ($controller, currentCommit) {
        //given
        currentCommit.id = undefined;

        var currentScope = {};

        //when
        $controller('CommitDetailsCtrl', {$scope: currentScope});

        //then
        //no request to backend was done
    }));
});
