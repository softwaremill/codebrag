'use strict';

describe("Follow-ups Controller", function () {

    var $rootScope, $q, scope;



    beforeEach(module('codebrag.followups'));

    beforeEach(inject(function(_$rootScope_, _$q_) {
        $rootScope = _$rootScope_;
        scope = $rootScope.$new();
        $q = _$q_;
    }));

    it('should fetch follow-ups from server', inject(function ($controller, followupsService) {
        // Given
        spyOn(followupsService, 'allFollowups').andReturn($q.when());

        // When
        $controller('FollowupsCtrl', {$scope: scope});

        //Then
        expect(followupsService.allFollowups).toHaveBeenCalled();
    }));

    it('should make loaded followups available in scope', inject(function ($controller, followupsService) {
        // Given
        var followups = 'some followups';
        spyOn(followupsService, 'allFollowups').andReturn($q.when(followups));

        // When
        $controller('FollowupsCtrl', {$scope: scope});
        $rootScope.$apply();

        //Then
        expect(scope.followupCommits).toBe(followups);
    }));

    it('should re-initialize controller when event received', inject(function($controller, events, followupsService) {
        // given
        var spy = spyOn(followupsService, 'allFollowups').andReturn($q.when());

        // when
        $controller('FollowupsCtrl', {$scope: scope});
        spy.reset();
        $rootScope.$broadcast(events.followupsTabOpened);

        // then
        expect(followupsService.allFollowups).toHaveBeenCalled();
    }));


});
