describe('Counters Service', function() {

    var branchesService,
        countersService,
        events,
        $rootScope,
        $injector,
        $timeout,
        $http,
        $q;

    var EXPECTED_INITIAL_COUNTERS = {commits: 10, followups: 20};
    var EXPECTED_WAITING_COUNTERS = {commits: 100, followups: 200};
    var COUNTERS_URL = 'rest/updates?branch=master';

    beforeEach(module('codebrag.counters'));

    beforeEach(inject(function(_$q_, _$rootScope_, _$httpBackend_, _branchesService_, _$injector_, _$timeout_, _events_) {
        branchesService = _branchesService_;
        events = _events_;
        $injector = _$injector_;
        $rootScope = _$rootScope_;
        $http = _$httpBackend_;
        $timeout = _$timeout_;
        $q = _$q_;
    }));

    afterEach(function() {
        $http.verifyNoOutstandingRequest();
        $http.verifyNoOutstandingExpectation();
    });

    describe('when branch service not ready', function() {

        it('should not initialize counters', function() {
            // given
            var notYetReady = $q.defer().promise;
            spyOn(branchesService, 'ready').andReturn(notYetReady);

            // when
            countersService = $injector.get('countersService');

            // then
            expect(currentCommitsCount()).toBe(0);
            expect(currentFollowupsCount()).toBe(0);
        });

    });

    describe('initialization when branch service ready', function() {

        beforeEach(function() {
            spyOn(branchesService, 'ready').andReturn($q.when());
            spyOn(branchesService, 'selectedBranch').andReturn('master');
        });

        it('should load and apply counters from server', function() {
            // given
            $http.expectGET(COUNTERS_URL).respond(EXPECTED_INITIAL_COUNTERS);

            // when
            countersService = $injector.get('countersService');
            $http.flush();

            // then
            expect(currentCommitsCount()).toBe(EXPECTED_INITIAL_COUNTERS.commits);
            expect(currentFollowupsCount()).toBe(EXPECTED_INITIAL_COUNTERS.followups);
        });

        it('should schedule next sync after current is done', function() {
            // given
            $http.whenGET(COUNTERS_URL).respond(EXPECTED_INITIAL_COUNTERS);

            // when
            var countersService = $injector.get('countersService');
            $http.flush();
            $http.whenGET(COUNTERS_URL).respond(EXPECTED_INITIAL_COUNTERS);

            // then
            $timeout.flush();
            $http.flush();
        });

        it('should not schedule next sync before current sync finished', function() {
            // given
            $http.expectGET(COUNTERS_URL).respond({});

            // when
            $injector.get('countersService');
            // no $http.flush() - we don't want to finish current call

            // then
            $timeout.verifyNoPendingTasks();
        });

        it('should schedule next sync when current call failed', function() {
            // given
            $http.expectGET(COUNTERS_URL).respond(500);

            // when
            $injector.get('countersService');

            // then
            $http.flush();
            $http.expectGET(COUNTERS_URL).respond(EXPECTED_INITIAL_COUNTERS);
            $timeout.flush();
            $http.flush();
        });

    });

    describe('when initial counters are loaded', function() {

        beforeEach(function() {
            spyOn(branchesService, 'ready').andReturn($q.when());
            spyOn(branchesService, 'selectedBranch').andReturn('master');
            $http.expectGET('rest/updates?branch=master').respond(EXPECTED_INITIAL_COUNTERS);
            countersService = $injector.get('countersService');
            $http.flush();
        });

        it('should have updates available', function() {
            // when
            $http.expectGET('rest/updates?branch=master').respond(EXPECTED_WAITING_COUNTERS);
            $timeout.flush();
            $http.flush();

            // then
            expect(currentCommitsCount()).toBe(EXPECTED_INITIAL_COUNTERS.commits);
            expect(currentFollowupsCount()).toBe(EXPECTED_INITIAL_COUNTERS.followups);
            expect(commitsUpdateAvailable()).toBeTruthy();
            expect(followupsUpdateAvailable()).toBeTruthy();
        });

        it('should have no updates when new counters not changed', function() {
            // when
            $http.expectGET('rest/updates?branch=master').respond(EXPECTED_INITIAL_COUNTERS);
            $timeout.flush();
            $http.flush();

            // then
            expect(commitsUpdateAvailable()).toBeFalsy();
            expect(followupsUpdateAvailable()).toBeFalsy();
        });

        it('should decrease commits count and have no updates when commit reviewed', function() {
            // when
            $rootScope.$broadcast(events.commitReviewed);

            // then
            expect(currentCommitsCount()).toBe(EXPECTED_INITIAL_COUNTERS.commits - 1);
            expect(currentFollowupsCount()).toBe(EXPECTED_INITIAL_COUNTERS.followups);
            expect(commitsUpdateAvailable()).toBeFalsy();
            expect(followupsUpdateAvailable()).toBeFalsy();
        });

        it('should decrease followups count when followup done', function() {
            // when
            $rootScope.$broadcast(events.followupDone);

            // then
            expect(currentCommitsCount()).toBe(EXPECTED_INITIAL_COUNTERS.commits);
            expect(currentFollowupsCount()).toBe(EXPECTED_INITIAL_COUNTERS.followups - 1);
            expect(commitsUpdateAvailable()).toBeFalsy();
            expect(followupsUpdateAvailable()).toBeFalsy();
        });

        testAdditionalCommitsLoaded('next');
        testAdditionalCommitsLoaded('previous');

        function testAdditionalCommitsLoaded(which) {
            it('should replace only commit counter with updated ones when ' + which + ' commits are loaded', function() {
                // given
                $http.expectGET('rest/updates?branch=master').respond(EXPECTED_WAITING_COUNTERS);
                $timeout.flush();
                $http.flush();

                // when
                $rootScope.$broadcast(events[which + 'CommitsLoaded']);

                // then
                expect(currentCommitsCount()).toBe(EXPECTED_WAITING_COUNTERS.commits);
                expect(currentFollowupsCount()).toBe(EXPECTED_INITIAL_COUNTERS.followups);
                expect(commitsUpdateAvailable()).toBeFalsy();
                expect(followupsUpdateAvailable()).toBeTruthy();
            });
        }
    });

    describe('when branch changed', function() {

        beforeEach(function() {
            spyOn(branchesService, 'ready').andReturn($q.when());
            spyOn(branchesService, 'selectedBranch').andReturn('master');
            $http.whenGET(COUNTERS_URL).respond(EXPECTED_INITIAL_COUNTERS);
            countersService = $injector.get('countersService');
            $http.flush();
        });

        it('should reinitialize only commits counter for new branch', function() {
            // when
            $http.expectGET('rest/updates?branch=feature').respond({commits: 50, followups: 500});
            $rootScope.$broadcast(events.branches.branchChanged, 'feature');
            $http.flush();

            // then
            expect(currentCommitsCount()).toBe(50);
            expect(currentFollowupsCount()).toBe(EXPECTED_INITIAL_COUNTERS.followups);
        });

    });

    function currentCommitsCount() {
        return countersService.commitsCounter.currentCount();
    }

    function currentFollowupsCount() {
        return countersService.followupsCounter.currentCount();
    }

    function commitsUpdateAvailable() {
        return countersService.commitsCounter.updateAvailable();
    }

    function followupsUpdateAvailable() {
        return countersService.followupsCounter.updateAvailable();
    }
});