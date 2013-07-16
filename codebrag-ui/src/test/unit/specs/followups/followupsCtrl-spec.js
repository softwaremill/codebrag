'use strict';

describe("Follow-ups Controller", function () {

    var scope = {};

    beforeEach(module('codebrag.followups'));

    it('should fetch follow-ups from server', inject(function ($controller, followupsService) {
        // Given
        spyOn(followupsService, 'allFollowups');

        // When
        $controller('FollowupsCtrl', {$scope: scope});

        //Then
        expect(followupsService.allFollowups).toHaveBeenCalled();
    }));

    it('should make loaded followups available in scope', inject(function ($controller, followupsService) {
        // Given
        var followups = 'some followups';
        spyOn(followupsService, 'allFollowups').andReturn(followups);

        // When
        $controller('FollowupsCtrl', {$scope: scope});

        //Then
        expect(scope.followupCommits).toBe(followups);
    }));

});
