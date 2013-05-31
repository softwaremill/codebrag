'use strict';

describe("Commits Controller", function () {

    var allCommits = '[all commits here]';
    var pendingCommits = '[pending commits here]';

    var scope;

    beforeEach(module('codebrag.commits'));

    beforeEach(function() {
        scope = {};
    });

    it('should fetch pending commits', inject(function($controller, commitsListService) {
        // Given
        var spy = spyOn(commitsListService, 'loadCommitsPendingReview');
        $controller('CommitsCtrl', {$scope: scope});

        // When
        scope.loadPendingCommits();

        expect(spy.callCount).toBe(2);
    }));

    it('should fetch additional commits', inject(function($controller, commitsListService) {
        // Given
        var loadPendingSpy = spyOn(commitsListService, 'loadCommitsPendingReview');
        var loadMoreSpy = spyOn(commitsListService, 'loadMoreCommits');
        $controller('CommitsCtrl', {$scope: scope});

        // When
        scope.loadMoreCommits();

        expect(loadPendingSpy.callCount).toBe(1);
        expect(loadMoreSpy.callCount).toBe(1);
    }));

    it('should fetch all commits', inject(function($controller, commitsListService) {
        // Given
        spyOn(commitsListService, 'loadCommitsPendingReview');    // called on controller start
        spyOn(commitsListService, 'loadAllCommits').andReturn(allCommits);

        // When
        $controller('CommitsCtrl', {$scope: scope});
        scope.loadAllCommits();

        expect(commitsListService.loadAllCommits).toHaveBeenCalled();
    }));

    it('should fetch commits pending review when controller starts', inject(function($controller, commitsListService) {
        // Given
        spyOn(commitsListService, 'loadCommitsPendingReview');

        // When
        $controller('CommitsCtrl', {$scope: scope});

        //Then
        expect(commitsListService.loadCommitsPendingReview).toHaveBeenCalled();
    }));

    it('should expose loading commits via scope', inject(function($controller, commitsListService) {
        // Given
        spyOn(commitsListService, 'loadCommitsPendingReview').andReturn(pendingCommits);

        // When
        $controller('CommitsCtrl', {$scope: scope});

        //Then
        expect(scope.commits).toBe(pendingCommits);
    }));

});
