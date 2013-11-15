describe('Page tour service', function() {

    var $rootScope, $compile, $document, authService, $q, pageTourService, userSettingsService, events;

    var USER_WITH_TOUR_NOT_YET_DONE = {
        settings: { appTourDone: false}
    };

    var USER_WITH_TOUR_COMPLETED = {
        settings: { appTourDone: true}
    };

    beforeEach(module('codebrag.templates'));
    beforeEach(module('codebrag.tour'));

    beforeEach(inject(function (_$rootScope_, _$compile_, _$document_, _authService_, _$q_, _pageTourService_, _userSettingsService_, _events_) {
        $compile = _$compile_;
        $rootScope = _$rootScope_;
        $document = _$document_;
        authService = _authService_;
        $q = _$q_;
        pageTourService = _pageTourService_;
        userSettingsService = _userSettingsService_;
        events = _events_;
    }));

    afterEach(function() {
        // cleanup document body after each spec
        $document.find('body').find('page-tour').remove();
    });

    it('should append tour element to DOM when user logs in and has not seen tour yet', function() {
        // given
        pageTourService.initializeTour();

        // when
        logUserIn(USER_WITH_TOUR_NOT_YET_DONE);

        // then
        expect(tourElementPresent()).toBeTruthy();
    });

    it('should not append tour element to DOM when user logs in and already completed tour', function() {
        // given
        pageTourService.initializeTour();

        // when
        logUserIn(USER_WITH_TOUR_COMPLETED);

        // then
        expect(tourElementPresent()).toBeFalsy();
    });

    it('should remove tour element from DOM when user finished tour', function() {
        // given
        pageTourService.initializeTour();
        spyOn(userSettingsService, 'save');
        logUserIn(USER_WITH_TOUR_NOT_YET_DONE);

        // when
        pageTourService.finishTour();

        // then
        expect(tourElementPresent()).toBeFalsy();
        expect(userSettingsService.save).toHaveBeenCalled();
    });

    it('should mark step as done', function() {
        // given
        expect(pageTourService.stepActive('commits')).toBeTruthy();

        // when
        pageTourService.ackStep('commits');

        // then
        expect(pageTourService.stepActive('commits')).toBeFalsy();
    });

    it('should mark invites step as active only when others are done', function() {
        // given
        expect(pageTourService.stepActive('invites')).toBeFalsy();

        // when
        pageTourService.ackStep('commits');
        pageTourService.ackStep('followups');

        // then
        expect(pageTourService.stepActive('invites')).toBeTruthy();
    });

    it('should reset tour steps to initial state when user logs in', function() {
        // given
        pageTourService.ackStep('commits');

        // when
        var anyUser = {};
        logUserIn(anyUser);

        // then
        expect(pageTourService.stepActive('commits')).toBeTruthy();
    });

    function tourElementPresent() {
        return $document.find('page-tour').length == 1;
    }

    function logUserIn(user) {
        spyOn(authService, 'requestCurrentUser').andReturn($q.when(user));
        $rootScope.$broadcast(events.loggedIn);
        $rootScope.$apply();
    }


});