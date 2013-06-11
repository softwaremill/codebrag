'use strict';

describe("Commits Controller", function () {

    var pendingCommits = ['commit1', 'commit2'];

    var scope;

    beforeEach(module('codebrag.commits'));

    beforeEach(inject(function ($rootScope) {
        scope = $rootScope;
    }));

    it('should transition to commit list state after switching to "all" mode', inject(function ($controller, commitsListService, $state) {
        // Given
        spyOn($state, "transitionTo");
        $controller('CommitsCtrl', {$scope: scope});

        // When
        scope.switchToAll();

        //Then
        expect($state.transitionTo).toHaveBeenCalledWith('commits.list');
    }));

    it('should transition to commit list state after switching to "pending" mode', inject(function ($controller, commitsListService, $state) {
        // Given
        spyOn($state, "transitionTo");
        $controller('CommitsCtrl', {$scope: scope});

        // When
        scope.switchToPending();

        //Then
        expect($state.transitionTo).toHaveBeenCalledWith('commits.list');
    }));

    it('should fetch pending commits', inject(function ($controller, commitsListService, $q) {
        // Given
        var spy = spyOn(commitsListService, 'loadCommitsPendingReview').andReturn(irrelevantPromise($q));
        $controller('CommitsCtrl', {$scope: scope});

        // When
        scope.loadPendingCommits();

        expect(spy.callCount).toBe(2);
    }));

    it('should fetch additional commits', inject(function ($controller, commitsListService, $q) {
        // Given
        var loadPendingSpy = spyOn(commitsListService, 'loadCommitsPendingReview').andReturn(irrelevantPromise($q));
        var loadMoreSpy = spyOn(commitsListService, 'loadMoreCommits').andReturn(irrelevantPromise($q));
        $controller('CommitsCtrl', {$scope: scope});

        // When
        scope.loadMoreCommits();

        expect(loadPendingSpy.callCount).toBe(1);
        expect(loadMoreSpy.callCount).toBe(1);
    }));

    it('should fetch all commits', inject(function ($controller, commitsListService, $q) {
        // Given
        spyOn(commitsListService, 'loadCommitsPendingReview').andReturn(irrelevantPromise($q));    // called on controller start
        spyOn(commitsListService, 'loadAllCommits').andReturn(irrelevantPromise($q));

        // When
        $controller('CommitsCtrl', {$scope: scope});
        scope.loadAllCommits();

        expect(commitsListService.loadAllCommits).toHaveBeenCalled();
    }));

    it('should fetch commits pending review when controller starts', inject(function ($controller, commitsListService, $q) {
        // Given
        spyOn(commitsListService, 'loadCommitsPendingReview').andReturn(irrelevantPromise($q));

        // When
        $controller('CommitsCtrl', {$scope: scope});

        //Then
        expect(commitsListService.loadCommitsPendingReview).toHaveBeenCalled();
    }));

    it('should expose loading commits via scope', inject(function ($controller, commitsListService, $q) {
        // Given
        spyOn(commitsListService, 'loadCommitsPendingReview').andReturn(promiseOfArray($q, pendingCommits));

        // When
        $controller('CommitsCtrl', {$scope: scope});
        scope.$apply();

        //Then
        expect(scope.commits).toBe(pendingCommits);
    }));

    it('should allow loading more in pending mode when there is more elements than loaded',
        inject(function ($controller, commitLoadFilter, commitsListService, $q) {

            // Given
            var loadedElementCount = 2;
            spyOn(commitsListService, 'loadCommitsPendingReview').andReturn(
                promiseOfArray($q, commitArrayOfSize(loadedElementCount)));
            $controller('CommitsCtrl', {$scope: scope});
            spyOn(commitLoadFilter, 'isAll').andReturn(false);

            // When
            scope.toReviewCount = loadedElementCount + 2;
            scope.$apply();

            // Then
            expect(scope.canLoadMore()).toBeTruthy();
        }));

    it('should not allow loading more in pending mode when loaded commit list size is equal to total size',
        inject(function ($controller, commitLoadFilter, commitsListService, $q) {
            // Given
            var loadedElementCount = 16;
            spyOn(commitsListService, 'loadCommitsPendingReview').andReturn(
                promiseOfArray($q, commitArrayOfSize(loadedElementCount)));
            $controller('CommitsCtrl', {$scope: scope});
            spyOn(commitLoadFilter, 'isAll').andReturn(false);

            // When
            scope.toReviewCount = loadedElementCount;
            scope.$apply();

            // Then
            expect(scope.canLoadMore()).toBeFalsy();
        }));

    it('should not allow loading more commits in "all" mode', inject(function ($controller, commitLoadFilter) {
        // Given
        spyOn(commitLoadFilter, 'isAll').andReturn(true);
        $controller('CommitsCtrl', {$scope: scope});

        // When
        var canLoad = scope.canLoadMore();

        // Then
        expect(canLoad).toBeFalsy();
    }));

    it('should update total commit count on event', inject(function ($controller, events, $rootScope) {
        // Given
        var newCommitCount = 20;
        $controller('CommitsCtrl', {$scope: scope});

        // When
        $rootScope.$broadcast(events.commitCountChanged, {commitCount: newCommitCount});

        // Then
        expect(scope.toReviewCount).toEqual(newCommitCount);
    }));

    function commitArrayOfSize(size) {
        var array = [];
        for (var i = 0; i< size; i++) array[i] = 'dummyCommit';
        return array;
    }

    function irrelevantPromise($q) {
        var deferred = $q.defer();
        deferred.resolve();
        return deferred.promise;
    }

    function promiseOfArray($q, array) {
        var deferred = $q.defer();
        deferred.resolve(array);
        return deferred.promise;
    }

});
