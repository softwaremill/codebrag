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
        var loadedCommits = commitArrayOfSize(3);
        var commitIdToRemove = 2;
        $httpBackend.whenGET('rest/commits?filter=pending').respond({commits:loadedCommits});
        $httpBackend.expectDELETE(commitUrl(commitIdToRemove)).respond(200);
        commitsListService.loadCommitsFromServer(commitLoadFilter.modes.pending);

        // When
        commitsListService.removeCommit(commitIdToRemove);
        $httpBackend.flush();

        // Then
        expect(commitsListService.allCommits().length).toBe(loadedCommits.length - 1);
    }));

    function givenServerReturns(commitsListService, loadedCommits, commitLoadFilter) {
        $httpBackend.whenGET('rest/commits?filter=pending').respond({commits:loadedCommits});
        commitsListService.loadCommitsFromServer(commitLoadFilter.modes.pending);
    }

    it('should not remove commit locally if filter is set to all', inject(function (commitsListService, commitLoadFilter) {
        // Given
        var loadedCommits = commitArrayOfSize(3);
        var commitIdToRemove = 2;
        givenServerReturns(commitsListService, loadedCommits, commitLoadFilter);
        $httpBackend.flush();

        // When
        commitLoadFilter.current = commitLoadFilter.modes.all;
        $httpBackend.expectDELETE(commitUrl(commitIdToRemove)).respond(200);
        commitsListService.removeCommit(commitIdToRemove);
        $httpBackend.flush();

        // Then
        expect(commitsListService.allCommits()[1].pendingReview).toBe(false);
        expect(commitsListService.allCommits()[0].pendingReview).toBe(true);
    }));

    it('should mark local commit as not pending review and get next one', inject(function (commitsListService, commitLoadFilter) {
        // Given
        var nextCommit = {};
        var loadedCommits = commitArrayOfSize(3);
        var commitIdToRemove = 2;
        givenServerReturns(commitsListService, loadedCommits, commitLoadFilter);
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
        expect(commitsListService.allCommits()[1].pendingReview).toBe(false);
        expect(commitsListService.allCommits()[0].pendingReview).toBe(true);
        expect(nextCommit.id).toBe('1');
    }));

    it('should load nreviewable commits from server', inject(function (commitsListService, commitLoadFilter) {
        // Given
        var loadedCommits = commitArrayOfSize(3);
        $httpBackend.whenGET('rest/commits?filter=pending').respond({commits:loadedCommits});

        // When
        commitsListService.loadCommitsFromServer(commitLoadFilter.modes.pending);
        $httpBackend.flush();

        // Then
        expect(commitsListService.allCommits().length).toBe(loadedCommits.length);
    }));

    it('should update notification count with number of reviewable commits', inject(function (commitsListService,
        commitLoadFilter, notificationCountersService) {
        // Given
        var loadedCommits = [commit(1), commit(2), notReviewable(commit(3))];
        $httpBackend.whenGET('rest/commits?filter=all').respond({commits:loadedCommits});
        spyOn(notificationCountersService, "updateCommits");

        // When
        commitsListService.loadCommitsFromServer(commitLoadFilter.modes.all);
        $httpBackend.flush();

        // Then
        expect(notificationCountersService.updateCommits).toHaveBeenCalledWith(2)
    }));

    it('should update notification count to zero if no commits returned from server', inject(function (commitsListService,
                                                                                              commitLoadFilter, notificationCountersService) {
        // Given
        var loadedCommits = [];
        $httpBackend.whenGET('rest/commits?filter=all').respond({commits:loadedCommits});
        spyOn(notificationCountersService, "updateCommits");

        // When
        commitsListService.loadCommitsFromServer(commitLoadFilter.modes.all);
        $httpBackend.flush();

        // Then
        expect(notificationCountersService.updateCommits).toHaveBeenCalledWith(0)
    }));

    it('should decrease commit notification count when deleting commit', inject(function (commitsListService,
                                                                                              commitLoadFilter, notificationCountersService) {
        // Given
        var loadedCommits = commitArrayOfSize(3);
        givenServerReturns(commitsListService, loadedCommits, commitLoadFilter);
        spyOn(notificationCountersService, "decreaseCommits");
        $httpBackend.expectDELETE('rest/commits/2').respond();

        // When
        commitsListService.removeCommit(2);
        $httpBackend.flush();

        // Then
        expect(notificationCountersService.decreaseCommits).toHaveBeenCalled()
    }));

    it('should decrease commit notification count when deleting commit and getting next', inject(function (commitsListService,
                                                                                                         commitLoadFilter, notificationCountersService) {
        // Given
        var loadedCommits = commitArrayOfSize(3);
        givenServerReturns(commitsListService, loadedCommits, commitLoadFilter);
        spyOn(notificationCountersService, "decreaseCommits");
        $httpBackend.expectDELETE('rest/commits/2').respond();

        // When
        commitsListService.removeCommitAndGetNext(2);
        $httpBackend.flush();

        // Then
        expect(notificationCountersService.decreaseCommits).toHaveBeenCalled()
    }));

    it('should call server to sync commits and add new ones to model', inject(function (commitsListService, commitLoadFilter) {
        // Given
        var loadedCommits = [commit(2)];
        $httpBackend.whenGET('rest/commits?filter=all').respond({commits:loadedCommits});
        var newCommits = [commit(2), commit(5)];
        $httpBackend.expectPOST('rest/commits/sync').respond({commits: newCommits});

        // When
        commitsListService.loadCommitsFromServer(commitLoadFilter.modes.all);
        commitsListService.syncCommits();
        $httpBackend.flush();

        // Then
        expect(commitsListService.allCommits()).toEqual(newCommits);
    }));

    it('should load all commits from server', inject(function (commitsListService, commitLoadFilter) {
        // Given
        var loadedCommits = [commit(1), commit(2), notReviewable(commit(3))];
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
