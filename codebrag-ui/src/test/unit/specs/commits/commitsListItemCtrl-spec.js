'use strict';

describe("CommitsListItemController", function () {

    var selectedCommit = {id: '123abc'};

    beforeEach(module('codebrag.commits'));

    it('should transition to commit details state with commit ID provided', inject(function($controller, $state) {
        // Given
        var scope = {};
        $controller('CommitsListItemCtrl', {$scope: scope});
        spyOn($state, "transitionTo");

        // When
        scope.openCommitDetails(selectedCommit);

        // Then
        expect($state.transitionTo).toHaveBeenCalledWith('commits.details', {id: selectedCommit.id})
    }));

    it('should transition to commit details when deleting commit returns next element', inject(function($controller, $state, $q, commitsListService, $rootScope) {
        // Given
        var scope = {};
        $controller('CommitsListItemCtrl', {$scope: scope});
        spyOn($state, "transitionTo");
        var deferred = $q.defer();
        deferred.resolve({id: 'nextId'});
        spyOn(commitsListService, 'removeCommitAndGetNext').andReturn(deferred.promise);

        // When
        scope.markAsReviewed(selectedCommit);
        $rootScope.$apply();

        // Then
        expect($state.transitionTo).toHaveBeenCalledWith('commits.details', {id: "nextId"})
    }));

    it('should transition to commit list when deleting last element', inject(function($controller, $state, $q, commitsListService, $rootScope) {
        // Given
        var scope = {};
        $controller('CommitsListItemCtrl', {$scope: scope});
        spyOn($state, "transitionTo");
        var deferred = $q.defer();
        deferred.resolve(null);
        spyOn(commitsListService, 'removeCommitAndGetNext').andReturn(deferred.promise);

        // When
        scope.markAsReviewed(selectedCommit);
        $rootScope.$apply();

        // Then
        expect($state.transitionTo).toHaveBeenCalledWith('commits.list')
    }));

    it('should remove given commit when marked as reviewed', inject(function($controller, $state, commitsListService, $q, $rootScope) {
        // Given
        var scope = {};
        var deferred = $q.defer();
        deferred.resolve({id: 'nextId'});
        $controller('CommitsListItemCtrl', {$scope: scope});
        spyOn(commitsListService, 'removeCommitAndGetNext').andReturn(deferred.promise);
        spyOn($state, "transitionTo");

        // When
        scope.markAsReviewed(selectedCommit);
        $rootScope.$apply();

        // Then
        expect(commitsListService.removeCommitAndGetNext).toHaveBeenCalledWith(selectedCommit.id);
    }));

});
