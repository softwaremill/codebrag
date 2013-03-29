'use strict';

describe("Session Controller", function () {

    beforeEach(module('codebrag.session'), module('codebrag.common.services'));

    afterEach(inject(function(_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));

    var scope, $httpBackend, ctrl, authSrv

    beforeEach(inject(function (_$httpBackend_, $rootScope, $routeParams, $controller, authService) {
        $httpBackend = _$httpBackend_;

        scope = $rootScope.$new();
        authSrv = authService
        ctrl = $controller('SessionCtrl', {$scope: scope, authService: authSrv});

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
        $httpBackend.expectPOST('rest/users').respond('anything');

        // When
        scope.login();
        $httpBackend.flush();
    });

    it('Should not call login rest service when form is invalid', function () {
        // Given
        scope.loginForm.$invalid = true;

        // When
        scope.login();

        // Then
        // verifyNoOutstandingRequest(); is checked after each test
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

    it('Calling logout should log out from angular layer', function () {
        // Given
        $httpBackend.expectGET('rest/users/logout').respond('');

        // When
        scope.logout();
        $httpBackend.flush();

    });
});
