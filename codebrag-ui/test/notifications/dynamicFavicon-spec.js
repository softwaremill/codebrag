ddescribe('Dynamic favicon directive', function() {

    var $rootScope, el, $compile, events;

    var faviconSelectors = 'link[rel="icon"], link[rel="shortcut icon"]';

    var initialDummyFaviconUrl = 'dummy.ico';
    var regularFaviconUrl = 'assets/images/favicon.ico';
    var notifFaviconUrl = 'assets/images/notification-favicon/favicon.ico';

    beforeEach(module('codebrag.notifications'));
    beforeEach(module('codebrag.events'));

    beforeEach(inject(function (_$rootScope_, _$compile_, _events_) {
        $compile = _$compile_;
        $rootScope = _$rootScope_;
        events = _events_;

        el = angular.element('<div>\n<link rel="icon" href="dummy.ico" dynamic-favicon/>\n<link rel="shortcut icon" href="dummy.ico" dynamic-favicon/>\n</div>');
        $compile(el)($rootScope);
    }));

    it('should not change favicon on application start', function() {
        var href = getFaviconUrl();
        expect(href).toBe(initialDummyFaviconUrl);
    });

    it('should change favicon when new updates are waiting users attention', function() {
        $rootScope.$broadcast(events.updatesWaiting, {commits: 10, followups: 10});
        var newHref = getFaviconUrl();
        expect(newHref).toBe(notifFaviconUrl);
    });

    it('should change favicon back to regular when commits updates were acknowledged', function() {
        setFaviconTo(notifFaviconUrl);
        $rootScope.$broadcast(events.commitCountChanged, {});
        var newHref = getFaviconUrl();
        expect(newHref).toBe(regularFaviconUrl);
    });

    it('should change favicon back to regular when followups updates were acknowledged', function() {
        setFaviconTo(notifFaviconUrl);
        $rootScope.$broadcast(events.followupCountChanged, {});
        var newHref = getFaviconUrl();
        expect(newHref).toBe(regularFaviconUrl);
    });

    function setFaviconTo(faviconUrl) {
        el.find(faviconSelectors).attr('href', faviconUrl)
    }

    function getFaviconUrl() {
        return el.find(faviconSelectors).attr('href');
    }

});