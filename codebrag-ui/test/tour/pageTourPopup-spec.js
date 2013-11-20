describe('Page tour popup', function() {

    var $rootScope, el, $compile, scope;

    beforeEach(module('codebrag.templates'));
    beforeEach(module('codebrag.tour'));

    beforeEach(inject(function (_$rootScope_, _$compile_) {
        $compile = _$compile_;
        $rootScope = _$rootScope_;
        scope = $rootScope.$new();
        el = angular.element(
            '<div>' +
                '<page-tour-popup ' +
                'visible-if="shouldDisplayPopup"' +
                'position-css-class="user-defined-position-class"' +
                'arrow-css-class="user-defined-arrow-class">' +
                    '<span>This is tour popup content</span>' +
                '</page-tour-popup>' +
            '</div>');
    }));

    it('should render popup content together with two buttons', function() {
        // when
        $compile(el)(scope);
        $rootScope.$apply();

        // then
        expect(getPopupContentText()).toBe('This is tour popup content');
    });

    it('add user-defined css classes to popup (defining position and arrow)', function() {
        // when
        $compile(el)(scope);
        $rootScope.$apply();

        // then
        var popup = getPopupBox();
        expect(popup.hasClass('user-defined-position-class')).toBeTruthy();
        expect(popup.hasClass('user-defined-arrow-class')).toBeTruthy();
    });

    it('displays popup according to user-defined condition', function() {
        // given
        scope.shouldDisplayPopup = true;

        // when
        $compile(el)(scope);
        $rootScope.$apply();

        // then
        expect(getPopupBox().css('display')).not.toBe('none');

        // and when
        scope.shouldDisplayPopup = false;
        $rootScope.$apply();

        // then
        expect(getPopupBox().css('display')).toBe('none');
    });

    function getPopupBox() {
        return el.find('.popup-box');
    }

    function getPopupContentText() {
        return el.find('.popup-box > div').text().trim();
    }

});