'use strict';

describe("Session Controller", function () {

    beforeEach(module('codebrag.session'), module('codebrag.common.services'));

    afterEach(inject(function(_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));

    var scope, $httpBackend, ctrl, authSrv, q

    beforeEach(inject(function (_$httpBackend_, $rootScope, $routeParams, $controller, authService, $q) {
        $httpBackend = _$httpBackend_;

        scope = $rootScope.$new();
        authSrv = authService
        ctrl = $controller('SessionCtrl', {$scope: scope, authService: authSrv});
        q = $q;

        scope.loginForm = {
            login: {
                $dirty: false
            },
            password: {
                $dirty: false
            },
            $invalid: false
        };
    }));

    it('Should call login rest service when form is valid', function () {
        // Given
        spyOn(authSrv, 'login').andReturn(q.when('anything'));

        // When
        scope.login();

        // Then
        expect(authSrv.login).toHaveBeenCalled();
    });

    it('Should not call login rest service when form is invalid', function () {
        // Given
        scope.loginForm.$invalid = true;
        spyOn(authSrv, 'login');

        // When
        scope.login();

        // Then
        expect(authSrv.login).not.toHaveBeenCalled()

    });

    it('Should have user not logged', function () {
        // Given
        // no user interaction was done before

        // Then
        expect(scope.isLogged()).toBe(false);
    });

    it('Should have user logged in', function () {
        // Given
        spyOn(authSrv, 'isAuthenticated').andReturn(true);

        // Then
        expect(scope.isLogged()).toBe(true);
    });

    it('Calling logout should log out from angular layer', inject(function ($state) {
        // Given
        spyOn($state, 'transitionTo');
        spyOn(authSrv, 'logout').andReturn(q.when('anything'));


        // When
        scope.logout();

        // Then
        expect(authSrv.logout).toHaveBeenCalled();
        expect()
    }));
});
