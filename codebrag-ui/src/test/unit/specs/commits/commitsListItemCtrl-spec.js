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

});
