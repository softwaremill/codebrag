'use strict';

describe("CommitsListItemController", function () {

    var selectedCommit = {id: '123abc'};
    var selectedCommitStateParams = {id: selectedCommit.id};
    var otherCommitStateParams = {id: '321cba'};

    beforeEach(module('codebrag.commits'));

    it('should transition to commit details state with commit ID provided', inject(function($controller, $state) {
        // Given
        var scope = {};
        $controller('CommitsListItemCtrl', {$scope: scope});
        spyOn($state, "transitionTo")

        // When
        scope.openCommitDetails(selectedCommit);

        // Then
        expect($state.transitionTo).toHaveBeenCalledWith('commits.details', {id: selectedCommit.id})
    }));

    it('should remove given commit when marked as reviewed', inject(function($controller, $state, commitsListService, $q, $rootScope) {
        // Given
        var scope = {};
        var deferred = $q.defer();
        deferred.resolve();
        $controller('CommitsListItemCtrl', {$scope: scope});
        spyOn(commitsListService, 'removeCommit').andReturn(deferred.promise);

        // When
        scope.markAsReviewed(selectedCommit);
        $rootScope.$apply();

        // Then
        expect(commitsListService.removeCommit).toHaveBeenCalledWith(selectedCommit.id);
    }));

    it('should get out of commit details when current commit is marked as reviewed', inject(function($controller, $state, commitsListService, $q, $rootScope) {
        // Given
        var scope = {};
        var deferred = $q.defer();
        deferred.resolve();
        $controller('CommitsListItemCtrl', {$scope: scope, $stateParams: selectedCommitStateParams});
        spyOn(commitsListService, 'removeCommit').andReturn(deferred.promise);
        spyOn($state, 'transitionTo');

        // When
        scope.markAsReviewed(selectedCommit);
        $rootScope.$apply();

        // Then
        expect($state.transitionTo).toHaveBeenCalledWith('commits.list');
    }));

    it('should not change commit details view when other commit is marked as reviewed', inject(function($controller, $state, commitsListService, $q, $rootScope) {
        // Given
        var scope = {};
        var deferred = $q.defer();
        deferred.resolve();
        $controller('CommitsListItemCtrl', {$scope: scope, $stateParams: otherCommitStateParams});
        spyOn(commitsListService, 'removeCommit').andReturn(deferred.promise);
        spyOn($state, 'transitionTo');

        // When
        scope.markAsReviewed(selectedCommit);
        $rootScope.$apply();

        // Then
        expect($state.transitionTo).not.toHaveBeenCalled();
    }));
});
