describe("Http service wrapper with unique requests handling", function () {

    var q;
    var httpMock;
    var rootScope;

    beforeEach(inject(function ($q, $rootScope) {
        q = $q;
        rootScope = $rootScope;
        httpMock = jasmine.createSpy('httpMock');
    }));

    it('should pass default request through', function() {
        // given
        var requestConfig = {method: 'GET', url: '/someUrl'};
        var httpWrapper = codebrag.uniqueRequestsAwareHttpService(httpMock, q);

        // when
        httpWrapper(requestConfig);

        // then
        expect(httpMock).toHaveBeenCalledWith(requestConfig);
    });

    it('should pass request when no pending request with the same requestId found', function() {
        // given
        var requestConfig = {method: 'GET', url: '/someUrl', unique: true, requestId: 'dummy'};
        httpMock.pendingRequests = [];
        var httpWrapper = codebrag.uniqueRequestsAwareHttpService(httpMock, q);

        // when
        httpWrapper(requestConfig);

        // then
        expect(httpMock).toHaveBeenCalledWith(requestConfig);
    });

    it('should not call original http service when pending request with the same requestId found', function() {
        // given
        var requestConfig = {method: 'GET', url: '/someUrl', unique: true, requestId: 'dummy'};
        httpMock.pendingRequests = [requestConfig];
        var httpWrapper = codebrag.uniqueRequestsAwareHttpService(httpMock, q);

        // when
        httpWrapper(requestConfig);

        // then
        expect(httpMock).not.toHaveBeenCalledWith(requestConfig);
    });


    it('should reject request if pending request with the same requestId found', function() {
        // given
        var requestConfig = {method: 'GET', url: '/someUrl', unique: true, requestId: 'dummy'};
        httpMock.pendingRequests = [requestConfig];
        var httpWrapper = codebrag.uniqueRequestsAwareHttpService(httpMock, q);

        // when
        var resultPromise = httpWrapper(requestConfig);

        // then
        var responseReceived;
        resultPromise.then(null, function(response) {
            responseReceived = response;
        });
        rootScope.$apply();
        expect(responseReceived.status).toEqual(499);
        expect(responseReceived.config).toEqual(requestConfig);
        expect(responseReceived.dropped).toBeTruthy();
    });

    it('should use decorated version for shortcut methods', function() {
        // given
        httpMock.pendingRequests = [{requestId: 'dummy'}];
        var httpWrapper = codebrag.uniqueRequestsAwareHttpService(httpMock, q);

        // when
        httpWrapper.get('/someUrl', {unique: true, requestId: 'dummy'});

        // then
        expect(httpMock).not.toHaveBeenCalled();

    });
});
