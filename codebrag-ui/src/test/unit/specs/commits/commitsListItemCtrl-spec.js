'use strict';

describe("CommitsListItemController", function () {

    var selectedCommit = {id: 123};

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

    it('should remove given commit when marked as reviewed', inject(function($controller, $state, commitsListService) {
        // Given
        var scope = {};
        $controller('CommitsListItemCtrl', {$scope: scope});
        spyOn(commitsListService, 'removeCommit');

        // When
        scope.markAsReviewed(selectedCommit);

        // Then
        expect(commitsListService.removeCommit).toHaveBeenCalledWith(selectedCommit.id);
    }));
});
