describe('Updates indicator directive', function() {

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

    function notificationVisible() {
        return el.find('span.ghost-notification').css('display') !== 'none';
    }

});