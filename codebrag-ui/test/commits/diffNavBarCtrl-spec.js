'use strict';

describe("DiffNavbarController", function () {

    var noopPromise = {then: function(){}};
    var $scope, $q, commitsService;

    var commit = {info: {sha: '123', repoName: 'codebrag', state: 'AwaitingUserReview'}};
    var nextCommit = {sha: '345', repoName: 'codebrag'};
    var currentRepoContext = {
        repo: 'codebrag'
    };

    beforeEach(module('codebrag.commits', function($provide) {
        $provide.value('currentRepoContext', currentRepoContext);
    }));

    beforeEach(inject(function($rootScope, _$q_, _commitsService_) {
        $scope = $rootScope.$new();
        $q = _$q_;
        commitsService = _commitsService_;
    }));

    it('should call service to mark current commit as reviewed', inject(function($controller, currentCommit) {
        // Given
        currentCommit.set(commit);
        spyOn(commitsService, 'markAsReviewed').andReturn(noopPromise);
        $controller('DiffNavbarCtrl', {$scope: $scope});
        $scope.$apply();

        // When
        $scope.markCurrentCommitAsReviewed();

        // Then
        expect(commitsService.markAsReviewed).toHaveBeenCalledWith(commit.info.sha);
    }));

    it('should go to next commit when making current commit reviewed', inject(function($controller, $state, currentCommit) {
        // Given
        spyOn(commitsService, 'markAsReviewed').andReturn($q.when(nextCommit));
        spyOn($state, 'transitionTo');
        currentCommit.set(commit);
        $controller('DiffNavbarCtrl', {$scope: $scope});
        $scope.$apply();

        // When
        $scope.markCurrentCommitAsReviewed();
        $scope.$apply();

        // Then
        expect($state.transitionTo).toHaveBeenCalledWith('commits.details', {sha: nextCommit.sha, repo: nextCommit.repoName } );
    }));

    it('should go to commits list when no next commit available', inject(function($controller, $state, currentCommit) {
        // Given
        var nextCommitDeferred = $q.defer();
        nextCommitDeferred.resolve(undefined);
        spyOn(commitsService, 'markAsReviewed').andReturn(nextCommitDeferred.promise);
        spyOn($state, 'transitionTo');
        currentCommit.set(commit);
        $controller('DiffNavbarCtrl', {$scope: $scope});
        $scope.$apply();

        // When
        $scope.markCurrentCommitAsReviewed();
        $scope.$apply();

        // Then
        expect($state.transitionTo).toHaveBeenCalledWith('commits.list', {repo: currentRepoContext.repo });
        expect(currentCommit.get()).toBeNull();
    }));

    it('should resolve readable status for current commit', inject(function($controller, currentCommit) {
        // Given
        currentCommit.set(commit);

        // When
        $controller('DiffNavbarCtrl', {$scope: $scope});
        $scope.$apply();

        // Then
        expect($scope.readableCommitStatus).toBe('Mark commit as reviewed');
    }));

});