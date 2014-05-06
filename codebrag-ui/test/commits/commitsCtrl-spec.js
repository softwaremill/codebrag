'use strict';

describe("Commits Controller", function () {

    var pendingCommits = ['commit1', 'commit2'];

    var $scope,
        $rootScope,
        $q,
        commitsService,
        branchesService,
        $stateParams,
        events,
        currentCommit;

    beforeEach(module('codebrag.commits', 'codebrag.notifications'));

    beforeEach(inject(function(_$rootScope_, _$q_, $controller, _commitsService_, _$stateParams_, _events_, _branchesService_, _currentCommit_) {
        $scope = _$rootScope_.$new();
        $rootScope = _$rootScope_;
        $q = _$q_;
        commitsService = _commitsService_;
        branchesService = _branchesService_;
        $stateParams = _$stateParams_;
        events = _events_;
        currentCommit = _currentCommit_;
    }));

    beforeEach(inject(function($controller) {
        spyOn(commitsService, 'loadCommits').andReturn($q.defer().promise);
        $controller('CommitsCtrl', {$scope: $scope, commitsListService: commitsService, $stateParams: $stateParams, branchesService: branchesService});
    }));

    it('should not load commits before branches service is ready', inject(function($controller) {
        // given
        var branchServiceReady = $q.defer();
        spyOn(branchesService, 'ready').andReturn(branchServiceReady.promise);

        // when
        $controller('CommitsCtrl', {$scope: $scope});

        // then
        expect(commitsService.loadCommits).not.toHaveBeenCalled();
    }));

    it('should load pending commits when branches service is ready', inject(function($controller) {
        // given
        var branchServiceReady = $q.defer();
        spyOn(branchesService, 'ready').andReturn(branchServiceReady.promise);

        // when
        $controller('CommitsCtrl', {$scope: $scope});
        branchServiceReady.resolve();
        $rootScope.$digest();

        // then
        expect(commitsService.loadCommits).toHaveBeenCalled();
    }));

    it('should re-load commits when "filter changed" event received', inject(function(currentCommit) {
        // given
        currentCommit.set('dummy commit');

        // when
        $rootScope.$broadcast(events.commitsListFilterChanged);

        // then
        expect(commitsService.loadCommits).toHaveBeenCalled();
    }));

    it('should load pending commits when view switched to pending', function() {
        // given
        commitsService.loadCommits.reset(); // reset spy call counter

        // When
        $rootScope.$broadcast(events.commitsListFilterChanged, 'pending');

        // then
        expect(commitsService.loadCommits.callCount).toBe(1);
    });

    it('should expose loaded commits to scope', function() {
        // Given
        var commits = $q.defer();
        commits.resolve(pendingCommits);
        commitsService.loadCommits.andReturn(commits.promise);

        // When
        $rootScope.$broadcast(events.commitsListFilterChanged, 'pending');
        $scope.$apply();

        //Then
        expect($scope.commits).toBe(pendingCommits);
    });

    it('should load newest commits in all mode when no commit is selected', function() {
        // given
        $stateParams.sha = null;

        // when
        $rootScope.$broadcast(events.commitsListFilterChanged, 'all');

        // then
        expect(commitsService.loadCommits).toHaveBeenCalledWith(null);
    });

    it('should load commits in all context when commit is selected', function() {
        // given
        $stateParams.sha = '123';

        // when
        $rootScope.$broadcast(events.commitsListFilterChanged, 'all');

        // then
        expect(commitsService.loadCommits).toHaveBeenCalledWith($stateParams.sha);
    });

    it('should indicate when all commits were reviewed', function() {
        // Given
        $scope.commits = [];
        spyOn(commitsService, 'hasNextCommits').andReturn(false);

        // When
        var result = $scope.allCommitsReviewed();

        expect(result).toBeTruthy();
    });

    it('should indicate when there is more commits to review on server', function() {
        // Given
        spyOn(commitsService, 'hasNextCommits').andReturn(true);

        // When
        var result = $scope.hasNextCommits();

        expect(result).toBeTruthy();
    });

    it('should clear current commit when opening another commit details', function() {
        // Given
        var sha = '123';
        currentCommit.set({info:{sha: '1234'}});
        spyOn(currentCommit, 'empty');

        // When
        $scope.openCommitDetails(sha);

        // Then
        expect(currentCommit.empty).toHaveBeenCalled();
    });

    it('should not open commit details if current commits attemtps to be reopened', function() {
        // Given
        var sha = '123';
        currentCommit.set({info:{sha: '123'}});
        spyOn(currentCommit, 'empty');

        // When
        $scope.openCommitDetails(sha);

        // Then
        expect(currentCommit.empty).not.toHaveBeenCalled();
    });

});
