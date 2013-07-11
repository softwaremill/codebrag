'use strict';

describe("Follow-ups Controller", function () {

    var scope = {};

    beforeEach(module('codebrag.followups'));

    it('should fetch follow-ups from server', inject(function ($controller, followupsListService) {
        // Given
        spyOn(followupsListService, 'loadFollowupGroupsFromServer');

        // When
        $controller('FollowupsCtrl', {$scope: scope});

        //Then
        expect(followupsListService.loadFollowupGroupsFromServer).toHaveBeenCalled();
    }));

    it('should make loaded followups available in scope', inject(function ($controller, followupsListService) {
        // Given
        var followups = 'some followups';
        spyOn(followupsListService, 'loadFollowupGroupsFromServer').andReturn(followups);

        // When
        $controller('FollowupsCtrl', {$scope: scope});

        //Then
        expect(scope.followupCommits).toBe(followups);
    }));

});
