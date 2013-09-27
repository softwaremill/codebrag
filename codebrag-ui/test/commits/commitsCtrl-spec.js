'use strict';

describe("Commits Controller", function () {

    var pendingCommits = ['commit1', 'commit2'];

    var $scope,
        $rootScope,
        $q,
        commitsListService,
        $stateParams,
        events;

    beforeEach(module('codebrag.commits'));

    beforeEach(inject(function(_$rootScope_, _$q_, $controller, _commitsListService_, _$stateParams_, _events_) {
        $scope = _$rootScope_.$new();
        $rootScope = _$rootScope_;
        $q = _$q_;
        commitsListService = _commitsListService_;
        $stateParams = _$stateParams_;
        events = _events_;
    }));

    beforeEach(inject(function($controller) {
        spyOn(commitsListService, 'loadCommitsToReview').andReturn($q.defer().promise);
        $controller('CommitsCtrl', {$scope: $scope, commitsListService: commitsListService, $stateParams: $stateParams});
    }));

    it('should have initial list mode set to pending', function() {
        expect($scope.listViewMode).toBe('pending');
        expect(commitsListService.loadCommitsToReview).toHaveBeenCalled();
    });

    it('should re-initialize controller when event received', inject(function(currentCommit) {
        // given
        $scope.listViewMode = 'all';
        currentCommit.set('dummy commit');

        // when
        $rootScope.$broadcast(events.reloadCommitsList);

        // then
        expect($scope.listViewMode).toBe('pending');
        expect(currentCommit.get()).toBeNull();
        expect(commitsListService.loadCommitsToReview).toHaveBeenCalled();
    }));

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

    it('should load newest commits in all mode when no commit is selected', function() {
        // given
        $stateParams.id = null;
        spyOn(commitsListService, 'loadNewestCommits');

        // when
        $scope.listViewMode = 'all';
        $scope.switchListView();

        // then
        expect(commitsListService.loadNewestCommits).toHaveBeenCalled();
    });

    it('should load commits in all context when commit is selected', function() {
        // given
        $stateParams.id = '123';
        spyOn(commitsListService, 'loadCommitsInContext');

        // when
        $scope.listViewMode = 'all';
        $scope.switchListView();

        // then
        expect(commitsListService.loadCommitsInContext).toHaveBeenCalled();
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

});
