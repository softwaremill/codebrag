ddescribe('Updates indicator directive', function() {

    var $rootScope, el, $compile, events;

    beforeEach(module('codebrag.notifications'));
    beforeEach(module('codebrag.events'));

    beforeEach(inject(function (_$rootScope_, _$compile_, _events_) {
        $compile = _$compile_;
        $rootScope = _$rootScope_;
        events = _events_;

        el = angular.element('<div>\n<span>8 commits</span>\n<update-indicator watch="commits"/>\n</div>');
        $compile(el)($rootScope);
        $rootScope.$digest();
    }));

    it('should have indicators hidden on start', function() {
        expect(notificationVisible()).toBe(false);
    });

    it('should show indicators when updates are waiting', function() {
        // when
        $rootScope.$broadcast(events.updatesWaiting, {commits: 10});
        $rootScope.$digest();

        // then
        expect(notificationVisible()).toBe(true);
    });

    it('should not show indicators when false updates are waiting (e.g. 0 value)', function() {
        // when
        $rootScope.$broadcast(events.updatesWaiting, {commits: 0});
        $rootScope.$digest();

        // then
        expect(notificationVisible()).toBe(false);
    });

    it('should hide indicators when counters change', function() {
        // given
        setNotificationVisible();

        // when
        $rootScope.$broadcast(events.commitCountChanged);
        $rootScope.$digest();

        // then
        expect(notificationVisible()).toBe(false);
    });

    it('should not hide indicators on other counter updates', function() {
        // given
        setNotificationVisible();

        // when
        $rootScope.$broadcast(events.followupCountChanged);
        $rootScope.$digest();

        // then
        expect(notificationVisible()).toBe(true);
    });

    function notificationVisible() {
        return el.find('span.ghost-notification').css('display') !== 'none';
    }

    function setNotificationVisible() {
        el.find('span.ghost-notification').css('display', 'inline');
    }

});