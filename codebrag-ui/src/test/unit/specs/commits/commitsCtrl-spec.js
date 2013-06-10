'use strict';

describe("Commits Controller", function () {

    var allCommits = '[all commits here]';
    var pendingCommits = '[pending commits here]';

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

    it('should fetch pending commits', inject(function ($controller, commitsListService) {
        // Given
        var spy = spyOn(commitsListService, 'loadCommitsPendingReview');
        $controller('CommitsCtrl', {$scope: scope});

        // When
        scope.loadPendingCommits();

        expect(spy.callCount).toBe(2);
    }));

    it('should fetch additional commits', inject(function ($controller, commitsListService) {
        // Given
        var loadPendingSpy = spyOn(commitsListService, 'loadCommitsPendingReview');
        var loadMoreSpy = spyOn(commitsListService, 'loadMoreCommits');
        $controller('CommitsCtrl', {$scope: scope});

        // When
        scope.loadMoreCommits();

        expect(loadPendingSpy.callCount).toBe(1);
        expect(loadMoreSpy.callCount).toBe(1);
    }));

    it('should fetch all commits', inject(function ($controller, commitsListService) {
        // Given
        spyOn(commitsListService, 'loadCommitsPendingReview');    // called on controller start
        spyOn(commitsListService, 'loadAllCommits').andReturn(allCommits);

        // When
        $controller('CommitsCtrl', {$scope: scope});
        scope.loadAllCommits();

        expect(commitsListService.loadAllCommits).toHaveBeenCalled();
    }));

    it('should fetch commits pending review when controller starts', inject(function ($controller, commitsListService) {
        // Given
        spyOn(commitsListService, 'loadCommitsPendingReview');

        // When
        $controller('CommitsCtrl', {$scope: scope});

        //Then
        expect(commitsListService.loadCommitsPendingReview).toHaveBeenCalled();
    }));

    it('should expose loading commits via scope', inject(function ($controller, commitsListService) {
        // Given
        spyOn(commitsListService, 'loadCommitsPendingReview').andReturn(pendingCommits);

        // When
        $controller('CommitsCtrl', {$scope: scope});

        //Then
        expect(scope.commits).toBe(pendingCommits);
    }));

    it('should allow loading more in pending mode when there is more elements than max',
        inject(function ($controller, events, $rootScope, commitLoadFilter) {
            // Given
            var maxElements = 7;
            $controller('CommitsCtrl', {$scope: scope});
            spyOn(commitLoadFilter, 'maxCommitsOnList').andReturn(maxElements);
            spyOn(commitLoadFilter, 'isAll').andReturn(false);

            // When
            scope.toReviewCount = 8;

            // Then
            expect(scope.canLoadMore()).toBeTruthy();
        }));

    it('should not allow loading more in pending mode when there is no more elements than max',
        inject(function ($controller, events, $rootScope, commitLoadFilter) {
            // Given
            var maxElements = 17;
            $controller('CommitsCtrl', {$scope: scope});
            spyOn(commitLoadFilter, 'maxCommitsOnList').andReturn(maxElements);
            spyOn(commitLoadFilter, 'isAll').andReturn(false);

            // When
            scope.toReviewCount = 8;

            // Then
            expect(scope.canLoadMore()).toBeFalsy();
        }));

    it('should not allow loading more commits in "all" mode', inject(function ($controller, commitsListService, commitLoadFilter) {
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
});
