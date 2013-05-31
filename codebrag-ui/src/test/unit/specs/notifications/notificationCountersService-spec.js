'use strict';

describe("Notification counters service", function () {

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

    it('should load data from server on logon event', inject(function (notificationCountersService, events) {
        // Given
        $httpBackend.expectGET('rest/notificationCounts').respond({pendingCommitCount: 0, followupCount: 0});
        rootScope.$broadcast(events.loggedIn);

        // When
        notificationCountersService.counters();
        $httpBackend.flush();

        // Then expected server url called only once
    }));

    it('should load counter values returned from server', inject(function (notificationCountersService, events) {
        // Given
        var expectedCommitCount = 15;
        var expectedFollowupCount = 121;
        $httpBackend.expectGET('rest/notificationCounts').respond({
                pendingCommitCount: expectedCommitCount,
                followupCount: expectedFollowupCount
            });
        rootScope.$broadcast(events.loggedIn);

        // When
        var counters = notificationCountersService.counters();
        $httpBackend.flush();

        // Then
        expect(counters.commits).toEqual(expectedCommitCount);
        expect(counters.followups).toEqual(expectedFollowupCount);
    }));

    it('should correctly update counter values on commit counter change event', inject(function (notificationCountersService, events) {
        // Given
        var expectedCommitCount = 15;
        var counters = notificationCountersService.counters();

        // When
        rootScope.$broadcast(events.commitCountChanged, {commitCount: expectedCommitCount});

        // Then
        expect(counters.commits).toEqual(expectedCommitCount);
    }));

    it('should correctly update counter values on commit reviewed event', inject(function (notificationCountersService, events) {
        // Given
        var initialCommitCount = 15;
        var counters = notificationCountersService.counters();
        rootScope.$broadcast(events.commitCountChanged, {commitCount: initialCommitCount});

        // When
        rootScope.$broadcast(events.commitReviewed);

        // Then
        expect(counters.commits).toEqual(initialCommitCount - 1);
    }));

    it('should correctly update counter values on follow-up counter change event', inject(function (notificationCountersService, events) {
        // Given
        var expectedFollowupCount = 15;
        var counters = notificationCountersService.counters();

        // When
        rootScope.$broadcast(events.followupCountChanged, {followupCount: expectedFollowupCount});

        // Then
        expect(counters.followups).toEqual(expectedFollowupCount);
    }));

});
