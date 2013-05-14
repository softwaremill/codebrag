'use strict';

describe("CommitsListService", function () {

    var $httpBackend;
    var rootScope;

    beforeEach(module('codebrag.notifications'));

    beforeEach(inject(function (_$httpBackend_, $rootScope) {
        $httpBackend = _$httpBackend_;
        rootScope = $rootScope;
    }));

    afterEach(inject(function (_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));

    it('should call server only on first load', inject(function (notificationCountersService) {
        // Given
        $httpBackend.expectGET('rest/notificationCounts').respond({pendingCommitCount: 0, followupCount: 0});

        // When
        notificationCountersService.counters();
        $httpBackend.flush();
        notificationCountersService.counters();

        // Then expected server url called only once
    }));

    it('should load counter values returned from server', inject(function (notificationCountersService) {
        // Given
        var expectedCommitCount = 15;
        var expectedFollowupCount = 121;
        $httpBackend.expectGET('rest/notificationCounts').respond({
                pendingCommitCount: expectedCommitCount,
                followupCount: expectedFollowupCount
            });

        // When
        var counters = notificationCountersService.counters();
        $httpBackend.flush();

        // Then
        expect(counters.commits).toEqual(expectedCommitCount);
        expect(counters.followups).toEqual(expectedFollowupCount);
    }));

    it('should correctly update counter values', inject(function (notificationCountersService) {
        // Given
        var expectedCommitCount = 15;
        var expectedFollowupCount = 121;
        $httpBackend.expectGET('rest/notificationCounts').respond({
            pendingCommitCount: 1,
            followupCount: 2
        });
        var counters = notificationCountersService.counters();
        $httpBackend.flush();

        // When
        notificationCountersService.updateCommits(expectedCommitCount);
        notificationCountersService.updateFollowups(expectedFollowupCount);

        // Then
        expect(counters.commits).toEqual(expectedCommitCount);
        expect(counters.followups).toEqual(expectedFollowupCount);
    }));

    it('should correctly decrease counter values', inject(function (notificationCountersService) {
        // Given
        var initialCommitCount = 15;
        var initialFollowupCount = 121;
        $httpBackend.expectGET('rest/notificationCounts').respond({
            pendingCommitCount: initialCommitCount,
            followupCount: initialFollowupCount
        });
        var counters = notificationCountersService.counters();
        $httpBackend.flush();

        // When
        notificationCountersService.decreaseCommits();
        notificationCountersService.decreaseFollowups();

        // Then
        expect(counters.commits).toEqual(initialCommitCount - 1);
        expect(counters.followups).toEqual(initialFollowupCount - 1);
    }));
});
