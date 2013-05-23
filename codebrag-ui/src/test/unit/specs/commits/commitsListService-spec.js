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
        $httpBackend.expectDELETE(commitUrl(commitIdToRemove)).respond(200);
        commitsListService.loadCommitsPendingReview();

        // When
        commitsListService.removeCommit(commitIdToRemove);
        $httpBackend.flush();

        // Then
        expect(commitsListService.allCommits().length).toBe(loadedCommits.length - 1);
    }));

    function givenServerReturnsPendingCommits(commitsListService, loadedCommits) {
        $httpBackend.whenGET('rest/commits?filter=pending').respond({commits:loadedCommits});
        commitsListService.loadCommitsPendingReview();
    }

    function givenServerReturnsAllCommits(commitsListService, loadedCommits) {
        $httpBackend.whenGET('rest/commits?filter=all').respond({commits:loadedCommits});
        commitsListService.loadAllCommits();
    }

    it('should not remove commit locally if filter is set to all', inject(function (commitsListService) {
        // Given
        var loadedCommits = commitArrayOfSize(3);
        var commitIdToRemove = 2;
        givenServerReturnsAllCommits(commitsListService, loadedCommits);
        $httpBackend.flush();

        // When
        $httpBackend.expectDELETE(commitUrl(commitIdToRemove)).respond(200);
        commitsListService.removeCommit(commitIdToRemove);
        $httpBackend.flush();

        // Then
        expect(commitsListService.allCommits()[1].pendingReview).toBe(false);
        expect(commitsListService.allCommits()[0].pendingReview).toBe(true);
    }));

    it('should mark local commit as not pending review and get next one', inject(function (commitsListService) {
        // Given
        var nextCommit = {};
        var loadedCommits = commitArrayOfSize(3);
        var commitIdToRemove = 2;
        givenServerReturnsAllCommits(commitsListService, loadedCommits);
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
        expect(nextCommit.id).toBe('1');
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

    it('should broadcast new number of reviewable commits after loading data data', inject(function(commitsListService) {
        // Given
        var loadedCommits = [commit(1), commit(2), notReviewable(commit(3))];
        $httpBackend.whenGET('rest/commits?filter=all').respond({commits:loadedCommits});
        var listener = jasmine.createSpy('listener');
        rootScope.$on('codebrag:commitCountChanged', listener);

        // When
        commitsListService.loadAllCommits();
        $httpBackend.flush();

        // Then
        expect(listener).toHaveBeenCalledWith(jasmine.any(Object), {commitCount: 2});
    }));

    it('should broadcast event with count zero if no commits returned from server', inject(function (commitsListService) {
        // Given
        var loadedCommits = [];
        $httpBackend.whenGET('rest/commits?filter=all').respond({commits:loadedCommits});
        var listener = jasmine.createSpy('listener');
        rootScope.$on('codebrag:commitCountChanged', listener);

        // When
        commitsListService.loadAllCommits();
        $httpBackend.flush();

        // Then
        expect(listener).toHaveBeenCalledWith(jasmine.any(Object), {commitCount: 0});
        expect(listener.callCount).toBe(1)
    }));

    it('should broadcast new commit count when deleting commit and getting next', inject(function (commitsListService) {
        // Given
        var loadedCommits = commitArrayOfSize(3);
        givenServerReturnsAllCommits(commitsListService, loadedCommits);
        $httpBackend.expectDELETE('rest/commits/2').respond();
        var listener = jasmine.createSpy('listener');
        rootScope.$on('codebrag:commitCountChanged', listener);

        // When
        commitsListService.removeCommitAndGetNext(2);
        $httpBackend.flush();

        // Then
        expect(listener).toHaveBeenCalledWith(jasmine.any(Object), {commitCount: 2});
        //expect(listener.callCount).toBe(1)
    }));

    it('should broadcast new commit count when deleting commit', inject(function (commitsListService) {
        // Given
        var loadedCommits = commitArrayOfSize(3);
        givenServerReturnsAllCommits(commitsListService, loadedCommits);
        $httpBackend.expectDELETE('rest/commits/2').respond();
        var listener = jasmine.createSpy('listener');
        rootScope.$on('codebrag:commitCountChanged', listener);

        // When
        commitsListService.removeCommit(2);
        $httpBackend.flush();

        // Then
        expect(listener).toHaveBeenCalledWith(jasmine.any(Object), {commitCount: 2});
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

});
