'use strict';

describe("Follow-ups Controller", function () {

    var scope = {};

    beforeEach(module('codebrag.followups'));

    it('should fetch follow-ups from server', inject(function ($controller, followupsListService) {
        // Given
        spyOn(followupsListService, 'loadFollowupsFromServer');

        // When
        $controller('FollowupsCtrl', {$scope: scope});

        //Then
        expect(followupsListService.loadFollowupsFromServer).toHaveBeenCalled();
    }));

    it('should make loaded followups available in scope', inject(function ($controller, followupsListService) {
        // Given
        var followups = 'some followups';
        spyOn(followupsListService, 'loadFollowupsFromServer').andReturn(followups);

        // When
        $controller('FollowupsCtrl', {$scope: scope});

        //Then
        expect(scope.followups).toBe(followups);
    }));

});
