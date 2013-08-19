'use strict';

describe("Commits Controller", function () {

    var pendingCommits = ['commit1', 'commit2'];

    var $scope,
        $q,
        commitsListService;

    beforeEach(module('codebrag.commits'));

    beforeEach(inject(function($rootScope, _$q_, $controller, _commitsListService_) {
        $scope = $rootScope.$new();
        $q = _$q_;
        commitsListService = _commitsListService_;
    }));

    beforeEach(inject(function($controller) {
        spyOn(commitsListService, 'loadCommitsToReview').andReturn($q.defer().promise);
        $controller('CommitsCtrl', {$scope: $scope, commitsListService: commitsListService});
    }));

    it('should have initial list mode set to pending', function() {
        expect($scope.listViewMode).toBe('pending');
        expect(commitsListService.loadCommitsToReview).toHaveBeenCalled();
    });

    it('should load pending commits when view switched to pending', function() {
        // given
        commitsListService.loadCommitsToReview.reset(); // reset spy call counter

        // When
        $scope.listViewMode = 'pending';
        $scope.switchListView();
        $scope.$apply();

        // then
        expect(commitsListService.loadCommitsToReview.callCount).toBe(1);
    });

    it('should expose loaded commits to scope', function() {
        // Given
        var commits = $q.defer();
        commits.resolve(pendingCommits);
        commitsListService.loadCommitsToReview.andReturn(commits.promise);

        // When
        $scope.listViewMode = 'pending';
        $scope.switchListView();
        $scope.$apply();

        //Then
        expect($scope.commits).toBe(pendingCommits);
    });

    it('should indicate when all commits were reviewed', function() {
        // Given
        $scope.commits = [];
        spyOn(commitsListService, 'hasNextCommits').andReturn(false);

        // When
        var result = $scope.allCommitsReviewed();

        expect(result).toBeTruthy();
    });

    it('should indicate when there is more commits to review on server', function() {
        // Given
        spyOn(commitsListService, 'hasNextCommits').andReturn(true);

        // When
        var result = $scope.hasNextCommits();

        expect(result).toBeTruthy();
    });

    it('should mark commit as reviewed and go to next if exists', inject(function($state) {
        // Given
        var data = {id: '123'};
        var nextCommit = $q.defer();
        nextCommit.resolve(data);
        spyOn(commitsListService, 'makeReviewedAndGetNext').andReturn(nextCommit.promise);
        spyOn($state, 'transitionTo');

        // When
        var result = $scope.markAsReviewed();
        $scope.$apply();

        expect($state.transitionTo).toHaveBeenCalledWith('commits.details', data);
    }));

    it('should mark commit as reviewed and go to empty details screen if no next commit found', inject(function($state) {
        // Given
        var noNextCommit = $q.defer();
        noNextCommit.resolve(undefined);
        spyOn(commitsListService, 'makeReviewedAndGetNext').andReturn(noNextCommit.promise);
        spyOn($state, 'transitionTo');

        // When
        var result = $scope.markAsReviewed();
        $scope.$apply();

        expect($state.transitionTo).toHaveBeenCalledWith('commits.list');
    }));

});
