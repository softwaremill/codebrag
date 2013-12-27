describe('Autocomplete-aware form submit directive', function() {

    var $rootScope, $scope, el, $compile;
    var template = "<div><form name=\"loginForm\" af-submit=\"login()\">\n    <input type=\"text\" name=\"username\" id=\"username\" ng-model=\"username\" data-af-enabled/>\n    <input type=\"password\" name=\"password\" id=\"password\" ng-model=\"password\" data-af-enabled/>\n    <input type=\"text\" name=\"notAfAware\" id=\"notAfAware\" ng-model=\"notAfAware\"/>\n</form></div>";

    var user = 'john';
    var pass = 'secret';

    beforeEach(module('codebrag.common.directives'));

    beforeEach(inject(function (_$rootScope_, _$compile_) {
        $compile = _$compile_;
        $rootScope = _$rootScope_;
        $scope = $rootScope.$new();
        el = angular.element(template);
        $compile(el)($scope);
        $scope.login = jasmine.createSpy();
    }));

    afterEach(function() {
        expect($scope.login).toHaveBeenCalled();
    });

    it('should submit form via form.submit', function() {
        el.find('form').submit();
    });

    it('model should be undefined when submitted with no values set', function() {
        // given
        // no values in inputs

        // when
        form(el).submit();

        // then
        expect($scope.username).toBeUndefined();
        expect($scope.password).toBeUndefined();
    });

    it('model should be set correctly when values set outside of Angular (e.g. browser-managed passwords)', function() {
        // given
        usernameInput(el).val(user);
        passwordInput(el).val(pass);

        // when
        form(el).submit();

        // then
        expect($scope.username).toBe(user);
        expect($scope.password).toBe(pass);
    });

    it('should not update bindings when field not is not ac-aware and value is set outside', function() {
        // given
        notAfAwareInput(el).val('this should have model value undefined');

        // when
        form(el).submit();

        // then
        expect($scope.notAfAware).toBeUndefined();
    });

    it('should update bindings when values set within angular lifecycle', function() {
        // given
        userTypes(user, usernameInput(el));
        userTypes('dummy value', notAfAwareInput(el));

        // when
        form(el).submit();

        // then
        expect($scope.username).toBe(user);
        expect($scope.notAfAware).toBe('dummy value');
    });

    function userTypes(value, input) {
        input.val(value);
        input.trigger('input');
    }

    function form(el) {
        return el.find('form');
    }

    function usernameInput(el) {
        return $(el[0]).find('#username');
    }

    function passwordInput(el) {
        return $(el[0]).find('#password');
    }

    function notAfAwareInput(el) {
        return $(el[0]).find('#notAfAware');
    }

});