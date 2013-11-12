describe('Page tour service', function() {

    var $rootScope, $compile, $document, authService, $q, pageTourService, userSettingsService;

    var USER_WITH_TOUR_NOT_YET_DONE = {
        settings: { welcomeFollowupDone: false}
    };

    var USER_WITH_TOUR_COMPLETED = {
        settings: { welcomeFollowupDone: true}
    };

    beforeEach(module('codebrag.templates'));
    beforeEach(module('codebrag.tour'));

    beforeEach(inject(function (_$rootScope_, _$compile_, _$document_, _authService_, _$q_, _pageTourService_, _userSettingsService_) {
        $compile = _$compile_;
        $rootScope = _$rootScope_;
        $document = _$document_;
        authService = _authService_;
        $q = _$q_;
        pageTourService = _pageTourService_;
        userSettingsService = _userSettingsService_;
    }));

    afterEach(function() {
        // cleanup document body after each spec
        $document.find('body').find('page-tour').remove();
    });

    it('should append tour element to DOM when user has not seen tour yet', function() {
        // given
        spyOn(authService, 'requestCurrentUser').andReturn($q.when(USER_WITH_TOUR_NOT_YET_DONE));

        // when
        pageTourService.startTour();
        $rootScope.$apply();

        // then
        expect(tourElementPresent()).toBeTruthy();
    });

    it('should not append tour element to DOM when user already completed tour', function() {
        // given
        spyOn(authService, 'requestCurrentUser').andReturn($q.when(USER_WITH_TOUR_COMPLETED));

        // when
        pageTourService.startTour();
        $rootScope.$apply();

        // then
        expect(tourElementPresent()).toBeFalsy();
    });

    it('should remove tour element from DOM when user finished tour', function() {
        // given
        spyOn(authService, 'requestCurrentUser').andReturn($q.when(USER_WITH_TOUR_NOT_YET_DONE));
        spyOn(userSettingsService, 'save');
        pageTourService.startTour();
        $rootScope.$apply();

        // when
        pageTourService.finishTour();

        // then
        expect(tourElementPresent()).toBeFalsy();
        expect(userSettingsService.save).toHaveBeenCalled();
    });

    it('should mark step as donw', function() {
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

    it('should reset tour steps to initial state when user logs in', inject(function(events) {
        // given
        pageTourService.ackStep('commits');

        // when
        $rootScope.$broadcast(events.loggedIn);
        $rootScope.$apply();

        // then
        expect(pageTourService.stepActive('commits')).toBeTruthy();
    }));

    function tourElementPresent() {
        return $document.find('page-tour').length == 1;
    }


});