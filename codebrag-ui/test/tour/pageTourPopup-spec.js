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
                'dismiss="popupDismissed()"' +
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
        expect(getOkButton().text()).toBe('Ok, got it');
        expect(getLearnMoreLink().text()).toBe('Learn more');
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

    it('should invoke user-defined function on OK button click', function() {
        // given
        scope.popupDismissed = jasmine.createSpy('popupDismissed');
        $compile(el)(scope);
        $rootScope.$apply();

        // when
        getOkButton().click();

        // then
        expect(scope.popupDismissed).toHaveBeenCalled();
    });

    function getPopupBox() {
        return el.find('.tour-info-box');
    }

    function getPopupContentText() {
        return el.find('.tour-info-box > div').text().trim();
    }

    function getOkButton() {
        return el.find('.tour-info-box-footer button').eq(0);
    }

    function getLearnMoreLink() {
        return el.find('.tour-info-box-footer a').eq(0);
    }

});