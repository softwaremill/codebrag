describe("Updates polling service", function () {

    var updatesPollingService;
    var $timeout, $httpBackend, $rootScope;

    var firstRequestUrl = '/rest/updates';
    var subsequentRequestsUrl = firstRequestUrl + '?since=';

    var respWithNoUpdates = {lastUpdateTimestamp: 123123123};
    var respWithUpdates = {
        lastUpdatedTimestamp: 456456456,
        commits: 5,
        followups: 10
    };

    beforeEach(module('codebrag.notifications'));

    beforeEach(inject(function(_updatesPollingService_, _$timeout_, _$httpBackend_, _$rootScope_) {
        updatesPollingService = _updatesPollingService_;
        $timeout = _$timeout_;
        $httpBackend = _$httpBackend_;
        $rootScope = _$rootScope_;
    }));

    afterEach(function() {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
    });


    it('should schedule call to server', function(){
        // given
        $httpBackend.expectGET(firstRequestUrl).respond(200, respWithNoUpdates);

        // when
        updatesPollingService.startPolling();
        flushAsync();
    });

    it('should schedule next call with last update time on succesful response', function(){
        // given
        $httpBackend.expectGET(firstRequestUrl).respond(200, respWithNoUpdates);

        // when
        updatesPollingService.startPolling();
        flushAsync();

        $httpBackend.expectGET(subsequentRequestsUrl + respWithNoUpdates.lastUpdateTimestamp).respond(200, respWithNoUpdates);
        flushAsync();
    });

    it('should schedule next call with no last update time on failed response', function(){
        // given
        $httpBackend.expectGET(firstRequestUrl).respond(400);

        // when
        updatesPollingService.startPolling();
        flushAsync();

        $httpBackend.expectGET(firstRequestUrl).respond(200, respWithNoUpdates);
        flushAsync();
    });

    it('should not include last check time in first request', function(){
        // given
        $httpBackend.expectGET(firstRequestUrl).respond(200, respWithNoUpdates);

        // when
        updatesPollingService.startPolling();
        flushAsync();
    });

    it('should include returned last check time in subsequent requests', function(){
        // given
        $httpBackend.expectGET(firstRequestUrl).respond(200, respWithNoUpdates);

        // when
        updatesPollingService.startPolling();
        flushAsync();

        $httpBackend.expectGET(subsequentRequestsUrl + respWithNoUpdates.lastUpdateTimestamp).respond(200, respWithNoUpdates);
        flushAsync();
    });

    it('should broadcast event when new updates received', function() {
        // given
        $httpBackend.expectGET(firstRequestUrl).respond(200, respWithUpdates);
        spyOn($rootScope, '$broadcast');

        //when
        updatesPollingService.startPolling();
        flushAsync();

        // then
        expect($rootScope.$broadcast).toHaveBeenCalledWith(updatesPollingService.updatesReceivedEvent, {commits: 5, followups: 10});
    });

    function flushAsync() {
        $timeout.flush();
        $httpBackend.flush();
    }
});
