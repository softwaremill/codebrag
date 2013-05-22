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

    it('should load data from server on logon event', inject(function (notificationCountersService) {
        // Given
        $httpBackend.expectGET('rest/notificationCounts').respond({pendingCommitCount: 0, followupCount: 0});
        rootScope.$broadcast("codebrag:loggedIn");

        // When
        notificationCountersService.counters();
        $httpBackend.flush();

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
        rootScope.$broadcast("codebrag:loggedIn");

        // When
        var counters = notificationCountersService.counters();
        $httpBackend.flush();

        // Then
        expect(counters.commits).toEqual(expectedCommitCount);
        expect(counters.followups).toEqual(expectedFollowupCount);
    }));

    it('should correctly update counter values on commit counter change event', inject(function (notificationCountersService) {
        // Given
        var expectedCommitCount = 15;
        var counters = notificationCountersService.counters();

        // When
        rootScope.$broadcast('codebrag:commitCountChanged', {commitCount: expectedCommitCount});

        // Then
        expect(counters.commits).toEqual(expectedCommitCount);
    }));

    it('should correctly update counter values on follow-up counter change event', inject(function (notificationCountersService) {
        // Given
        var expectedFollowupCount = 15;
        var counters = notificationCountersService.counters();

        // When
        rootScope.$broadcast('codebrag:followupCountChanged', {followupCount: expectedFollowupCount});

        // Then
        expect(counters.followups).toEqual(expectedFollowupCount);
    }));

});
