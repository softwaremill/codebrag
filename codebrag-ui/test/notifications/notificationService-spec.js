'use strict';

describe("Notification service", function () {

    var $httpBackend, $rootScope, $timeout;
    var notificationService, events, branchesService;
    
    var updatesUrl = function(branch) {
        var defaultBranch = 'master';
        if(branch) {
            return 'rest/updates?branch=' + branch;
        }
        return 'rest/updates?branch=' + defaultBranch;
    }

    beforeEach(module('codebrag.notifications', 'codebrag.branches'));

    beforeEach(inject(function (_$httpBackend_, _$rootScope_, _notificationService_, _events_, _$timeout_, _branchesService_) {
        $httpBackend = _$httpBackend_;
        $rootScope = _$rootScope_;
        notificationService = _notificationService_;
        branchesService = _branchesService_;
        events = _events_;
        $timeout = _$timeout_;
        spyOn(branchesService, 'selectedBranch').andReturn('master');
    }));

    afterEach(inject(function (_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));

    it('should load counter values on login', function () {
        // Given
        var expectedCommitCount = 15;
        var expectedFollowupCount = 121;
        $httpBackend.expectGET(updatesUrl()).respond({
            commits: expectedCommitCount,
            followups: expectedFollowupCount
        });
        $rootScope.$broadcast(events.loggedIn);

        // When
        var counters = notificationService.counters;
        $httpBackend.flush();

        // Then
        expect(counters.commitsCount).toEqual(expectedCommitCount);
        expect(counters.followupsCount).toEqual(expectedFollowupCount);
    });

    it('should schedule next update call on response', function () {
        // Given
        $httpBackend.expectGET(updatesUrl()).respond({commits: 0, followups: 0});

        // When
        $rootScope.$broadcast(events.loggedIn);
        $httpBackend.flush();

        // Then
        $httpBackend.expectGET(updatesUrl()).respond({commits: 0, followups: 0});
        $timeout.flush();
        $httpBackend.flush();
    });

    it('should broadcast update event with new commits and followups number', function () {
        // Given
        $httpBackend.expectGET(updatesUrl()).respond({commits: 2, followups: 5});
        $rootScope.$broadcast(events.loggedIn);
        $httpBackend.flush();
        $httpBackend.expectGET(updatesUrl()).respond({commits: 7, followups: 13});
        spyOn($rootScope, '$broadcast');

        // When
        $timeout.flush();
        $httpBackend.flush();

        // Then
        expect($rootScope.$broadcast).toHaveBeenCalledWith(events.updatesWaiting, {commits: 5, followups: 8});
    });

    it('should schedule next call from previous on succesful response', function () {
        // Given
        $httpBackend.expectGET(updatesUrl()).respond({commits: 2, followups: 5});
        $rootScope.$broadcast(events.loggedIn);
        $httpBackend.flush();
        $httpBackend.expectGET(updatesUrl()).respond({commits: 7, followups: 13});

        // When
        $timeout.flush();
        $httpBackend.flush();

        // Then
        $httpBackend.expectGET(updatesUrl()).respond({commits: 7, followups: 13});
        $timeout.flush();
        $httpBackend.flush();
    });

    it('should schedule next call from previous on error response', function () {
        // Given
        $httpBackend.expectGET(updatesUrl()).respond({commits: 2, followups: 5});
        $rootScope.$broadcast(events.loggedIn);
        $httpBackend.flush();
        $httpBackend.expectGET(updatesUrl()).respond(400, {commits: 7, followups: 13});

        // When
        $timeout.flush();
        $httpBackend.flush();

        // Then
        $httpBackend.expectGET(updatesUrl()).respond({commits: 7, followups: 13});
        $timeout.flush();
        $httpBackend.flush();
    });

    it('should decrease counter value when commit was reviewed and followup was done', function () {
        // Given
        var counters = notificationService.counters;
        counters.commitsCount = 10;
        counters.followupsCount = 20;

        // When
        $rootScope.$broadcast(events.commitReviewed);
        $rootScope.$broadcast(events.followupDone);

        // Then
        expect(counters.commitsCount).toEqual(9);
        expect(counters.followupsCount).toEqual(19);
    });

    it('should update commits count only and broadcast update event with followups only', function () {
        // Given
        var counters = notificationService.counters;
        counters.commitsCount = 10;
        counters.followupsCount = 20;
        $httpBackend.expectGET(updatesUrl()).respond({commits: 12, followups: 25});

        // When
        $rootScope.$broadcast(events.refreshCommitsCounter);
        spyOn($rootScope, '$broadcast');
        $httpBackend.flush();

        // Then
        expect($rootScope.$broadcast).toHaveBeenCalledWith(events.updatesWaiting, {commits: 0, followups: 5});
        expect(counters.commitsCount).toBe(12);
        expect(counters.followupsCount).toBe(20);   // followups counter not updated
    });

    it('should update followups count and broadcast update event with commits only', function () {
        // Given
        var counters = notificationService.counters;
        counters.commitsCount = 10;
        counters.followupsCount = 20;
        $httpBackend.expectGET(updatesUrl()).respond({commits: 12, followups: 25});

        // When
        $rootScope.$broadcast(events.refreshFollowupsCounter);
        spyOn($rootScope, '$broadcast');
        $httpBackend.flush();

        // Then
        expect($rootScope.$broadcast).toHaveBeenCalledWith(events.updatesWaiting, {commits: 2, followups: 0});
        expect(counters.commitsCount).toBe(10);     // commits counter not updated
        expect(counters.followupsCount).toBe(25);
    });
});
