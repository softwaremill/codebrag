describe("Follow-ups list service", function () {

    var $httpBackend;
    var rootScope;

    beforeEach(module('codebrag.followups'));

    beforeEach(inject(function (_$httpBackend_, $rootScope) {
        $httpBackend = _$httpBackend_;
        rootScope = $rootScope;
    }));

    afterEach(inject(function (_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));


    it('should update notification count with number of pending follow-ups', inject(function (followupsListService, notificationCountersService) {
        // Given
        var loadedFollowups = followupArrayOfSize(3);
        $httpBackend.whenGET('rest/followups/').respond({followups:loadedFollowups});
        spyOn(notificationCountersService, "updateFollowups");

        // When
        followupsListService.loadFollowupsFromServer();
        $httpBackend.flush();

        // Then
        expect(notificationCountersService.updateFollowups).toHaveBeenCalledWith(3)
    }));

    it('should decrease the number of follow-ups when removing', inject(function (followupsListService, notificationCountersService) {
        // Given
        var loadedFollowups = followupArrayOfSize(3);
        $httpBackend.whenGET('rest/followups/').respond({followups:loadedFollowups});
        followupsListService.loadFollowupsFromServer();
        $httpBackend.flush();
        $httpBackend.expectDELETE('rest/followups/1').respond();
        spyOn(notificationCountersService, "decreaseFollowups");

        // When
        followupsListService.removeFollowup(1);
        $httpBackend.flush();

        // Then
        expect(notificationCountersService.decreaseFollowups).toHaveBeenCalled()
    }));

    it('should decrease the number of follow-ups when removing and getting next', inject(function (followupsListService, notificationCountersService) {
        // Given
        var loadedFollowups = followupArrayOfSize(3);
        $httpBackend.whenGET('rest/followups/').respond({followups:loadedFollowups});
        followupsListService.loadFollowupsFromServer();
        $httpBackend.flush();
        $httpBackend.expectDELETE('rest/followups/1').respond();
        spyOn(notificationCountersService, "decreaseFollowups");

        // When
        followupsListService.removeFollowup(1);
        $httpBackend.flush();

        // Then
        expect(notificationCountersService.decreaseFollowups).toHaveBeenCalled()
    }));

    function followup(id) {
        var idStr = id.toString();
        return {
            followupId: idStr,
            userId: "userId",
            commit: {
               date: "date",
               commitId: "commitId",
               message: "message",
               authorName: "authorName"
            },
            comment: {
                commenterName: "commenterName",
                commentId: "commentId"
            },
            date: "date"
        }
    }

    function followupArrayOfSize(size) {
        var array = [];
        for (var i = 1; i < size + 1; i++) {
            array.push(followup(i))
        }
        return array
    }

});
