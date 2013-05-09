'use strict';

describe("CommitsListService", function () {

    var $httpBackend;
    var rootScope;

    beforeEach(module('codebrag.commits'));

    beforeEach(inject(function (_$httpBackend_, $rootScope) {
        $httpBackend = _$httpBackend_;
        rootScope = $rootScope;
    }));

    afterEach(inject(function (_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));

    it('should remove commit locally and from server', inject(function (commitsListService, commitLoadFilter) {
        // Given
        var loadedCommits = [commit(111), commit(222), commit(333)];
        var commitIdToRemove = 222;
        $httpBackend.whenGET('rest/commits?filter=pending').respond({commits:loadedCommits});
        $httpBackend.expectDELETE(commitUrl(commitIdToRemove)).respond(200);
        commitsListService.loadCommitsFromServer(commitLoadFilter.modes.pending);

        // When
        commitsListService.removeCommit(commitIdToRemove);
        $httpBackend.flush();

        // Then
        expect(commitsListService.allCommits().length).toBe(loadedCommits.length - 1);
    }));

    it('should not remove commit locally if filter is set to all', inject(function (commitsListService, commitLoadFilter) {
        // Given
        var loadedCommits = [commit(111), commit(222), commit(333)];
        var commitIdToRemove = 222;
        $httpBackend.whenGET('rest/commits?filter=pending').respond({commits:loadedCommits});
        commitsListService.loadCommitsFromServer(commitLoadFilter.modes.pending);
        $httpBackend.flush();

        // When
        commitLoadFilter.current = commitLoadFilter.modes.all;
        $httpBackend.expectDELETE(commitUrl(commitIdToRemove)).respond(200);
        commitsListService.removeCommit(commitIdToRemove);
        $httpBackend.flush();

        // Then
        expect(commitsListService.allCommits()).toEqual(loadedCommits);
        expect(commitsListService.allCommits()[1].pendingReview).toBe(false);
        expect(commitsListService.allCommits()[0].pendingReview).toBe(true);
    }));

    it('should mark local commit as not pending review and get next one', inject(function (commitsListService, commitLoadFilter) {
        // Given
        var nextCommit = {};
        var loadedCommits = [commit(111), commit(222), commit(333)];
        var commitIdToRemove = 222;
        $httpBackend.whenGET('rest/commits?filter=pending').respond({commits:loadedCommits});
        commitsListService.loadCommitsFromServer(commitLoadFilter.modes.pending);
        $httpBackend.flush();

        // When
        commitLoadFilter.current = commitLoadFilter.modes.all;
        $httpBackend.expectDELETE(commitUrl(commitIdToRemove)).respond(200);
        commitsListService.removeCommitAndGetNext(commitIdToRemove).then(
            function(returnedNext) {
                nextCommit = returnedNext
            }
        );
        $httpBackend.flush();

        // Then
        expect(commitsListService.allCommits()).toEqual(loadedCommits);
        expect(commitsListService.allCommits()[1].pendingReview).toBe(false);
        expect(commitsListService.allCommits()[0].pendingReview).toBe(true);
        expect(nextCommit.id).toBe('111');
    }));

    it('should load non-reviewable commits from server', inject(function (commitsListService, commitLoadFilter) {
        // Given
        var loadedCommits = [commit(111), commit(222), commit(333)];
        $httpBackend.whenGET('rest/commits?filter=pending').respond({commits:loadedCommits});

        // When
        commitsListService.loadCommitsFromServer(commitLoadFilter.modes.pending);
        $httpBackend.flush();

        // Then
        expect(commitsListService.allCommits().length).toBe(loadedCommits.length);
    }));

    it('should call server to sync commits and add new ones to model', inject(function (commitsListService, commitLoadFilter) {
        // Given
        var loadedCommits = [commit(222)];
        $httpBackend.whenGET('rest/commits?filter=all').respond({commits:loadedCommits});
        var newCommits = [commit(222), commit(555)];
        $httpBackend.expectPOST('rest/commits/sync').respond({commits: newCommits});

        // When
        commitsListService.loadCommitsFromServer(commitLoadFilter.modes.all);
        commitsListService.syncCommits();
        $httpBackend.flush();

        // Then
        expect(commitsListService.allCommits()).toEqual(newCommits);
    }));

    it('should load reviewed commits from server', inject(function (commitsListService, commitLoadFilter) {
        // Given
        var loadedCommits = [commit(111), commit(222), commit(333)];
        $httpBackend.whenGET('rest/commits?filter=all').respond({commits:loadedCommits});

        // When
        commitsListService.loadCommitsFromServer(commitLoadFilter.modes.all);
        $httpBackend.flush();

        // Then
        expect(commitsListService.allCommits().length).toBe(loadedCommits.length);
    }));

    it('should load commit by id', inject(function (commitsListService) {
        // Given
        var loadedCommit = {};
        var expectedCommit = {commit: {}};
        $httpBackend.whenGET('rest/commits/123').respond(expectedCommit);

        // When
        commitsListService.loadCommitById(123).then(function(data) {
            loadedCommit = data;
        });

        $httpBackend.flush();
        rootScope.$apply();

        // Then
        expect(loadedCommit).toBe(expectedCommit);
    }));

    function commitUrl(id) {
        return 'rest/commits/' + id;
    }

    function commit(id) {
        var idStr = id.toString();
        return {id: idStr, sha: 'sha' + idStr, msg: 'message' + idStr, pendingReview: true}
    }

});
