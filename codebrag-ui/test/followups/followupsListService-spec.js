describe("Followups service", function () {

    var $httpBackend;
    var rootScope;
    var followups;

    beforeEach(module('codebrag.followups'));

    beforeEach(inject(function (_$httpBackend_, $rootScope) {
        $httpBackend = _$httpBackend_;
        rootScope = $rootScope;
        followups = [
            {"commit": {"commitId": "commit_1"}, "followups": [
                {"followupId": "followup_11", "lastReaction": {"reactionId": "comment_11"}},
                {"followupId": "followup_12", "lastReaction": {"reactionId": "comment_12"}},
                {"followupId": "followup_13", "lastReaction": {"reactionId": "comment_13"}}
            ]},
            {"commit": {"commitId": "commit_2"}, "followups": [
                {"followupId": "followup_21", "lastReaction": {"reactionId": "comment_21"}},
                {"followupId": "followup_22", "lastReaction": {"reactionId": "comment_22"}}
            ]}
        ];
    }));

    afterEach(inject(function (_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));


    it('should broadcast update event after loading follow-ups', inject(function (followupsService, events) {
        // Given
        $httpBackend.whenGET('rest/followups/').respond({followupsByCommit: followups});
        var listener = jasmine.createSpy('listener');
        rootScope.$on(events.followupCountChanged, listener);

        // When
        followupsService.allFollowups();
        $httpBackend.flush();

        // Then
        expect(listener).toHaveBeenCalledWith(jasmine.any(Object), {followupCount: 5});
    }));

    it('should broadcast new number of follow-ups when removing', inject(function (followupsService, events) {
        // Given
        $httpBackend.whenGET('rest/followups/').respond({followupsByCommit: followups});
        followupsService.allFollowups();
        $httpBackend.flush();
        $httpBackend.expectDELETE('rest/followups/followup_11').respond();
        var listener = jasmine.createSpy('listener');
        rootScope.$on(events.followupCountChanged, listener);

        // When
        followupsService.removeAndGetNext('followup_11');
        $httpBackend.flush();

        // Then
        expect(listener).toHaveBeenCalledWith(jasmine.any(Object), {followupCount: 4});
    }));

});
