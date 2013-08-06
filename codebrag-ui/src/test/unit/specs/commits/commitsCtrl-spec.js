'use strict';

describe("Commits Controller", function () {

    var pendingCommits = ['commit1', 'commit2'];

    var scope;

    beforeEach(module('codebrag.commits'));

    beforeEach(inject(function ($rootScope) {
        scope = $rootScope;
    }));

    it('should have initial list mode set to pending', inject(function($controller, commitsListService) {
        // given
        var spy = spyOn(commitsListService, 'loadCommitsPendingReview');

        // when
       $controller('CommitsCtrl', {$scope: scope});

        // then
        expect(scope.listViewMode).toBe('pending');
        expect(spy.callCount).toBe(1);
    }));

    it('should fetch pending commits', inject(function ($controller, commitsListService) {
        // Given
        var spy = spyOn(commitsListService, 'loadCommitsPendingReview').andReturn(pendingCommits);
        $controller('CommitsCtrl', {$scope: scope});
        resetSpyCounter(commitsListService, 'loadCommitsPendingReview'); // reset spy after ctrl initialization

        // When
        scope.loadPendingCommits();

        expect(spy.callCount).toBe(1);
    }));

    it('should fetch all commits when not in commit context', inject(function ($controller, commitsListService, $stateParams) {
        // Given
        spyOn(commitsListService, 'loadCommitsPendingReview'); // called on controller start
        spyOn(commitsListService, 'loadAllCommits');
        delete $stateParams.id; // make sure $stateParams has no "id" property

        // When
        $controller('CommitsCtrl', {$scope: scope});
        scope.loadAllCommits();

        expect(commitsListService.loadAllCommits).toHaveBeenCalled();
    }));

    it('should fetch current commit with surroundings when context', inject(function ($controller, commitsListService, $stateParams) {
        // Given
        spyOn(commitsListService, 'loadCommitsPendingReview'); // called on controller start
        spyOn(commitsListService, 'loadSurroundings');
        $stateParams.id = '123';

        // When
        $controller('CommitsCtrl', {$scope: scope});
        scope.loadAllCommits();

        expect(commitsListService.loadSurroundings).toHaveBeenCalledWith('123');
    }));

    it('should fetch commits pending review when controller starts', inject(function ($controller, commitsListService) {
        // Given
        spyOn(commitsListService, 'loadCommitsPendingReview');

        // When
        $controller('CommitsCtrl', {$scope: scope});

        //Then
        expect(commitsListService.loadCommitsPendingReview).toHaveBeenCalled();
    }));

    it('should expose loaded commits to scope', inject(function ($controller, commitsListService) {
        // Given
        spyOn(commitsListService, 'loadCommitsPendingReview').andReturn(pendingCommits);

        // When
        $controller('CommitsCtrl', {$scope: scope});

        //Then
        expect(scope.commits).toBe(pendingCommits);
    }));

    function resetSpyCounter(target, methodName) {
        target[methodName].reset();
    }

//    it('should fetch additional commits', inject(function ($controller, commitsListService) {
//        // Given
//        var loadPendingSpy = spyOn(commitsListService, 'loadCommitsPendingReview').andReturn(pendingCommits);
//        var loadMoreSpy = spyOn(commitsListService, 'loadMoreCommits').andReturn(irrelevantPromise($q));
//        $controller('CommitsCtrl', {$scope: scope});
//
//        // When
//        scope.loadMoreCommits();
//
//        expect(loadPendingSpy.callCount).toBe(1);
//        expect(loadMoreSpy.callCount).toBe(1);
//    }));
//
//    it('should allow loading more in pending mode when there is more elements than loaded',
//        inject(function ($controller, commitLoadFilter, commitsListService, $q) {
//
//        // Given
//        var loadedElementCount = 2;
//        spyOn(commitsListService, 'loadCommitsPendingReview').andReturn(['commit one', 'commit two']);
//        spyOn(commitLoadFilter, 'isAll').andReturn(false);
//
//        $controller('CommitsCtrl', {$scope: scope});
//
//        // When
//        scope.toReviewCount = loadedElementCount + 2;
//
//        // Then
//        expect(scope.canLoadMore()).toBeTruthy();
//    }));
//
//    it('should not allow loading more in pending mode when loaded commit list size is equal to total size',
//        inject(function ($controller, commitLoadFilter, commitsListService, $q) {
//            // Given
//            var loadedElementCount = 16;
//            spyOn(commitsListService, 'loadCommitsPendingReview').andReturn(
//                promiseOfArray($q, commitArrayOfSize(loadedElementCount)));
//            $controller('CommitsCtrl', {$scope: scope});
//            spyOn(commitLoadFilter, 'isAll').andReturn(false);
//
//            // When
//            scope.toReviewCount = loadedElementCount;
//            scope.$apply();
//
//            // Then
//            expect(scope.canLoadMore()).toBeFalsy();
//        }));
//
//    it('should not allow loading more commits in "all" mode', inject(function ($controller, commitLoadFilter) {
//        // Given
//        spyOn(commitLoadFilter, 'isAll').andReturn(true);
//        $controller('CommitsCtrl', {$scope: scope});
//
//        // When
//        var canLoad = scope.canLoadMore();
//
//        // Then
//        expect(canLoad).toBeFalsy();
//    }));
//
//    it('should update total commit count on event', inject(function ($controller, events, $rootScope) {
//        // Given
//        var newCommitCount = 20;
//        $controller('CommitsCtrl', {$scope: scope});
//
//        // When
//        $rootScope.$broadcast(events.commitCountChanged, {commitCount: newCommitCount});
//
//        // Then
//        expect(scope.toReviewCount).toEqual(newCommitCount);
//    }));
//
//    function commitArrayOfSize(size) {
//        var array = [];
//        for (var i = 0; i< size; i++) array[i] = 'dummyCommit';
//        return array;
//    }
//
//    function irrelevantPromise($q) {
//        var deferred = $q.defer();
//        deferred.resolve();
//        return deferred.promise;
//    }
//
//    function promiseOfArray($q, array) {
//        var deferred = $q.defer();
//        deferred.resolve(array);
//        return deferred.promise;
//    }

});
