'use strict';

describe("DiffNavbarController", function () {

    var noopPromise = {then: function(){}};
    var $scope, $q, commitsListService;

    var commit = {info: {id: '123'}};
    var nextCommit = {id: '345'};

    beforeEach(module('codebrag.commits'));

    beforeEach(inject(function($rootScope, _$q_, _commitsListService_) {
        $scope = $rootScope.$new();
        $q = _$q_;
        commitsListService = _commitsListService_;
    }));

    it('should call service to mark current commit as reviewed', inject(function($controller, currentCommit) {
        // Given
        currentCommit.set(commit);
        spyOn(commitsListService, 'makeReviewedAndGetNext').andReturn(noopPromise);
        $controller('DiffNavbarCtrl', {$scope: $scope});
        $scope.$apply();

        // When
        $scope.markCurrentCommitAsReviewed();

        // Then
        expect(commitsListService.makeReviewedAndGetNext).toHaveBeenCalledWith(commit.info.id);
    }));

    it('should go to next commit when making current commit reviewed', inject(function($controller, $state, currentCommit) {
        // Given
        var nextCommitDeferred = $q.defer();
        nextCommitDeferred.resolve(nextCommit);
        spyOn(commitsListService, 'makeReviewedAndGetNext').andReturn(nextCommitDeferred.promise);
        spyOn($state, 'transitionTo');
        currentCommit.set(commit);
        $controller('DiffNavbarCtrl', {$scope: $scope});
        $scope.$apply();

        // When
        $scope.markCurrentCommitAsReviewed();
        $scope.$apply();

        // Then
        expect($state.transitionTo).toHaveBeenCalledWith('commits.details', nextCommit);
    }));

    it('should go to commits list when no next commit available', inject(function($controller, $state, currentCommit) {
        // Given
        var nextCommitDeferred = $q.defer();
        nextCommitDeferred.resolve(undefined);
        spyOn(commitsListService, 'makeReviewedAndGetNext').andReturn(nextCommitDeferred.promise);
        spyOn($state, 'transitionTo');
        currentCommit.set(commit);
        $controller('DiffNavbarCtrl', {$scope: $scope});
        $scope.$apply();

        // When
        $scope.markCurrentCommitAsReviewed();
        $scope.$apply();

        // Then
        expect($state.transitionTo).toHaveBeenCalledWith('commits.list');
        expect(currentCommit.get()).toBeNull();
    }));

});