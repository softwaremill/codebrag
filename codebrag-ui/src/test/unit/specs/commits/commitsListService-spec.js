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

    it('should remove commit locally and from server', inject(function (commitsListService) {
        // Given
        var loadedCommits = commitArrayOfSize(3);
        var commitIdToRemove = 2;
        $httpBackend.whenGET('rest/commits?filter=pending').respond({commits:loadedCommits});
        commitsListService.loadCommitsPendingReview();
        $httpBackend.flush();
        $httpBackend.expectDELETE(commitUrl(commitIdToRemove)).respond(200);
        $httpBackend.whenGET('rest/commits?filter=pending&limit=1&skip=2').respond({commits:[]});

        // When
        commitsListService.removeCommit(commitIdToRemove);
        $httpBackend.flush();

        // Then
        expect(commitsListService.allCommits().length).toBe(loadedCommits.length - 1);
        expect(commitsListService.allCommits()[0].id).toBe('1');
        expect(commitsListService.allCommits()[1].id).toBe('3');
    }));

    it('should load one new commit after deleting', inject(function (commitsListService) {
        // Given
        var loadedCommits = commitArrayOfSize(3);
        var commitIdToRemove = 2;
        $httpBackend.whenGET('rest/commits?filter=pending').respond({commits:loadedCommits});
        commitsListService.loadCommitsPendingReview();
        $httpBackend.flush();
        $httpBackend.expectDELETE(commitUrl(commitIdToRemove)).respond(200);
        $httpBackend.whenGET('rest/commits?filter=pending&limit=1&skip=2').respond({commits: [commit(4)]});

        // When
        commitsListService.removeCommit(commitIdToRemove);
        $httpBackend.flush();

        // Then
        expect(commitsListService.allCommits().length).toBe(loadedCommits.length);
        expect(commitsListService.allCommits()[0].id).toBe('1');
        expect(commitsListService.allCommits()[1].id).toBe('3');
        expect(commitsListService.allCommits()[2].id).toBe('4');
    }));

    it('should load one new commit before deleting and getting next', inject(function (commitsListService) {
        // Given
        var loadedCommits = commitArrayOfSize(3);
        var commitIdToRemove = 2;
        $httpBackend.whenGET('rest/commits?filter=pending').respond({commits:loadedCommits});
        commitsListService.loadCommitsPendingReview();
        $httpBackend.flush();
        $httpBackend.expectDELETE(commitUrl(commitIdToRemove)).respond(200);
        $httpBackend.whenGET('rest/commits?filter=pending&limit=1&skip=3').respond({commits: [commit(4)]});

        // When
        commitsListService.removeCommitAndGetNext(commitIdToRemove);
        $httpBackend.flush();

        // Then
        expect(commitsListService.allCommits().length).toBe(loadedCommits.length);
        expect(commitsListService.allCommits()[0].id).toBe('1');
        expect(commitsListService.allCommits()[1].id).toBe('3');
        expect(commitsListService.allCommits()[2].id).toBe('4');
    }));

    it('should not remove commit locally if filter is set to all', inject(function (commitsListService) {
        // Given
        var loadedCommits = commitArrayOfSize(3);
        var commitIdToRemove = 2;
        _givenServerReturnsAllCommits(commitsListService, loadedCommits);
        $httpBackend.flush();

        // When
        $httpBackend.expectDELETE(commitUrl(commitIdToRemove)).respond(200);
        commitsListService.removeCommit(commitIdToRemove);
        $httpBackend.flush();

        // Then
        expect(commitsListService.allCommits()[1].pendingReview).toBe(false);
        expect(commitsListService.allCommits()[0].pendingReview).toBe(true);
    }));

    it('should keep current commit index as next after removing in pending mode', inject(function (commitsListService) {
        // Given
        var nextCommit = {};
        var loadedCommits = commitArrayOfSize(4);
        var commitIdToRemove = 2;
        _givenServerReturnsPendingCommits(commitsListService, loadedCommits);
        $httpBackend.flush();
        $httpBackend.whenGET('rest/commits?filter=pending&limit=1&skip=4').respond({commits: []});

        // When
        $httpBackend.expectDELETE(commitUrl(commitIdToRemove)).respond(200);
        commitsListService.removeCommitAndGetNext(commitIdToRemove).then(
            function(returnedNext) {
                nextCommit = returnedNext
            }
        );
        $httpBackend.flush();

        // Then
        expect(nextCommit.id).toBe('3');
    }));

    it('should mark local commit as not pending', inject(function (commitsListService) {
        // Given
        var nextCommit = {};
        var loadedCommits = commitArrayOfSize(3);
        var commitIdToRemove = 2;
        _givenServerReturnsAllCommits(commitsListService, loadedCommits);
        $httpBackend.flush();

        // When
        $httpBackend.expectDELETE(commitUrl(commitIdToRemove)).respond(200);
        commitsListService.removeCommitAndGetNext(commitIdToRemove).then(
            function(returnedNext) {
                nextCommit = returnedNext
            }
        );
        $httpBackend.flush();

        // Then
        expect(commitsListService.allCommits()[1].pendingReview).toBe(false);
        expect(commitsListService.allCommits()[0].pendingReview).toBe(true);
    }));

    it('should load reviewable commits from server', inject(function (commitsListService) {
        // Given
        var loadedCommits = commitArrayOfSize(3);
        $httpBackend.whenGET('rest/commits?filter=pending').respond({commits:loadedCommits});

        // When
        commitsListService.loadCommitsPendingReview();
        $httpBackend.flush();

        // Then
        expect(commitsListService.allCommits().length).toBe(loadedCommits.length);
    }));

    it('should load additional commits from server', inject(function (commitsListService) {
        // Given
        var loadedCommits = commitArrayOfSize(3);
        $httpBackend.whenGET('rest/commits?filter=pending').respond({commits:loadedCommits});
        commitsListService.loadCommitsPendingReview();
        var additionalCommits = commitArrayOfSize(2);
        $httpBackend.flush();
        $httpBackend.whenGET('rest/commits?filter=pending&limit=7&skip=3').respond({commits:additionalCommits});

        // When
        commitsListService.loadMoreCommits();
        $httpBackend.flush();

        // Then
        expect(commitsListService.allCommits().length).toBe(loadedCommits.length + additionalCommits.length);
    }));

    it('should broadcast an event after loading pending commits', inject(function (commitsListService, events) {
        // Given
        var loadedCommits = commitArrayOfSize(3);
        $httpBackend.whenGET('rest/commits?filter=pending').respond({totalCount: 3, commits:loadedCommits});
        var listener = jasmine.createSpy('listener');
        rootScope.$on(events.commitCountChanged, listener);

        // When
        commitsListService.loadCommitsPendingReview();
        $httpBackend.flush();

        // Then
        expect(listener).toHaveBeenCalledWith(jasmine.any(Object), {commitCount: 3})
    }));

    it('should broadcast an event after loading all commits', inject(function (commitsListService, events) {
        // Given
        var loadedCommits = commitArrayOfSize(3);
        $httpBackend.whenGET('rest/commits?filter=all').respond({totalCount: 13, commits:loadedCommits});
        var listener = jasmine.createSpy('listener');
        rootScope.$on(events.commitCountChanged, listener);

        // When
        commitsListService.loadAllCommits();
        $httpBackend.flush();

        // Then
        expect(listener).toHaveBeenCalledWith(jasmine.any(Object), {commitCount: 13});
    }));

    it('should broadcast an event after loading additional commits', inject(function (commitsListService, events) {
        // Given
        var loadedCommits = commitArrayOfSize(3);
        $httpBackend.whenGET('rest/commits?filter=pending').respond({totalCount: 13, commits:loadedCommits});
        commitsListService.loadCommitsPendingReview();
        $httpBackend.flush();
        var moreCommits = commitArrayOfSize(4);
        $httpBackend.whenGET('rest/commits?filter=pending&limit=7&skip=3').respond({totalCount: 9, commits:moreCommits});
        var listener = jasmine.createSpy('listener');
        rootScope.$on(events.commitCountChanged, listener);

        // When
        commitsListService.loadMoreCommits();
        $httpBackend.flush();

        // Then
        expect(listener).toHaveBeenCalledWith(jasmine.any(Object), {commitCount: 9});
    }));

    it('should not change current commits when no additional commits are returned', inject(function (commitsListService) {
        // Given
        var loadedCommits = commitArrayOfSize(3);
        $httpBackend.whenGET('rest/commits?filter=pending').respond({commits:loadedCommits});
        commitsListService.loadCommitsPendingReview();
        var additionalCommits = [];
        $httpBackend.flush();
        $httpBackend.whenGET('rest/commits?filter=pending&limit=7&skip=3').respond({commits:additionalCommits});

        // When
        commitsListService.loadMoreCommits();
        $httpBackend.flush();

        // Then
        expect(commitsListService.allCommits().length).toBe(loadedCommits.length);
    }));

    it('should call server to sync commits and add new ones to model', inject(function (commitsListService) {
        // Given
        var loadedCommits = [commit(2)];
        $httpBackend.whenGET('rest/commits?filter=all').respond({commits:loadedCommits});
        var newCommits = [commit(2), commit(5)];
        $httpBackend.expectPOST('rest/commits/sync').respond({commits: newCommits});

        // When
        commitsListService.loadAllCommits();
        commitsListService.syncCommits();
        $httpBackend.flush();

        // Then
        expect(commitsListService.allCommits()).toEqual(newCommits);
    }));

    it('should load all commits from server', inject(function (commitsListService) {
        // Given
        var loadedCommits = [commit(1), commit(2), notReviewable(commit(3))];
        $httpBackend.whenGET('rest/commits?filter=all').respond({commits:loadedCommits});

        // When
        commitsListService.loadAllCommits();
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

    function notReviewable(commit) {
        commit.pendingReview = false;
        return commit;
    }

    function commitArrayOfSize(size) {
        var array = [];
        for (var i = 1; i < size + 1; i++) {
            array.push(commit(i))
        }
        return array;
    }

    function _givenServerReturnsAllCommits(commitsListService, loadedCommits) {
        $httpBackend.whenGET('rest/commits?filter=all').respond({commits:loadedCommits});
        commitsListService.loadAllCommits();
    }

    function _givenServerReturnsPendingCommits(commitsListService, loadedCommits) {
        $httpBackend.whenGET('rest/commits?filter=pending').respond({commits:loadedCommits});
        commitsListService.loadCommitsPendingReview();
    }

});
