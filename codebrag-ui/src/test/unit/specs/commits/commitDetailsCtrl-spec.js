'use strict';

describe("CommitDetailsController", function () {

    var selectedCommit = {id: 123, sha: '123abc123'};

    beforeEach(module('codebrag.commits'));

    it('should receive selected commit info from commits list element', inject(function($controller) {
        // Given
        var commitsListItemScope = {};
        var commitDetailsScope = {};
        $controller('CommitsListItemCtrl', {$scope: commitsListItemScope});
        $controller('CommitDetailsCtrl', {$scope: commitDetailsScope});

        // When
        commitsListItemScope.openCommitDetails(selectedCommit);

        // Then
        expect(commitDetailsScope.currentCommit.id).toEqual(selectedCommit.id);
        expect(commitDetailsScope.currentCommit.sha).toEqual(selectedCommit.sha);
    }));

});
