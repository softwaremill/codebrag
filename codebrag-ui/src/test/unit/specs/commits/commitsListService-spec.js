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

    it('remove commit locally and from server', inject(function (commitsListService) {
        // Given
        var loadedCommits = [commit(111), commit(222), commit(333)];
        var commitIdToRemove = 222;
        $httpBackend.whenGET('rest/commits?reviewed=false').respond({commits:loadedCommits});
        $httpBackend.expectDELETE(commitUrl(commitIdToRemove)).respond(200);
        commitsListService.loadCommitsFromServer(LOAD_MODE.ONLY_PENDING);

        // When
        commitsListService.removeCommit(commitIdToRemove);
        $httpBackend.flush();

        // Then
        expect(commitsListService.allCommits().length).toBe(loadedCommits.length - 1);
    }));

    it('should load non-reviewed commits from server', inject(function (commitsListService) {
        // Given
        var loadedCommits = [commit(111), commit(222), commit(333)];
        $httpBackend.whenGET('rest/commits?reviewed=false').respond({commits:loadedCommits});

        // When
        commitsListService.loadCommitsFromServer(LOAD_MODE.ONLY_PENDING);
        $httpBackend.flush();

        // Then
        expect(commitsListService.allCommits().length).toBe(loadedCommits.length);
    }));

    it('should call server to sync commits and add new ones to model', inject(function (commitsListService) {
        // Given
        var loadedCommits = [commit(222)];
        $httpBackend.whenGET('rest/commits?reviewed=true').respond({commits:loadedCommits});
        var newCommits = [commit(222), commit(555)];
        $httpBackend.expectPOST('rest/commits/sync').respond({commits: newCommits});

        // When
        commitsListService.loadCommitsFromServer(LOAD_MODE.WITH_REVIEWED);
        commitsListService.syncCommits();
        $httpBackend.flush();

        // Then
        expect(commitsListService.allCommits()).toEqual(newCommits);
    }));

    it('should load reviewed commits from server', inject(function (commitsListService) {
        // Given
        var loadedCommits = [commit(111), commit(222), commit(333)];
        $httpBackend.whenGET('rest/commits?reviewed=true').respond({commits:loadedCommits});

        // When
        commitsListService.loadCommitsFromServer(LOAD_MODE.WITH_REVIEWED);
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
        return {id: idStr, sha: 'sha' + idStr, msg: 'message' + idStr}
    }

    var LOAD_MODE = {
        WITH_REVIEWED: true,
        ONLY_PENDING: false
    }

});
