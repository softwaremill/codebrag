'use strict';

describe("CommitsListItemController", function () {

    var selectedCommit = {id: 123, sha: '123abc123'};

    beforeEach(module('codebrag.commits'));

    it('should update current commit information when commit was clicked', inject(function($controller, currentCommit) {
        // Given
        var scope = {};
        $controller('CommitsListItemCtrl', {$scope: scope, currentCommit: currentCommit});

        // When
        scope.openCommitDetails(selectedCommit);

        // Then
        expect(currentCommit.id).toEqual(selectedCommit.id);
        expect(currentCommit.sha).toEqual(selectedCommit.sha);
    }));

});
