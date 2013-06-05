'use strict';

describe("Session Controller", function () {

    beforeEach(module('codebrag.session'), module('codebrag.common.services'));

    afterEach(inject(function(_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));

    var scope, $httpBackend, ctrl, authSrv, q;

    beforeEach(inject(function (_$httpBackend_, $rootScope, $routeParams, $controller, authService, $q) {
        $httpBackend = _$httpBackend_;

        scope = $rootScope.$new();
        authSrv = authService;
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

    it('Should call login rest service when form is valid', inject(function ($state) {
        // Given
        spyOn(authSrv, 'login').andReturn(q.when('anything'));
        spyOn($state, 'transitionTo');

        // When
        scope.login();

        // Then
        expect(authSrv.login).toHaveBeenCalled();
    }));

    it('Should not call login rest service when form is invalid', inject(function ($state) {
        // Given
        scope.loginForm.$invalid = true;
        spyOn($state, 'transitionTo');
        spyOn(authSrv, 'login');

        // When
        scope.login();

        // Then
        expect(authSrv.login).not.toHaveBeenCalled()

    }));

    it('Should provide currently logged in user if authenticated', inject(function ($state, authService) {
        // Given
        spyOn($state, 'transitionTo');
        var expectedUser = {};
        spyOn(authService, 'isAuthenticated').andReturn(true);
        authService.loggedInUser = expectedUser;

        // When
        var loggedInUser = scope.loggedInUser();

        // Then
        expect(loggedInUser).toBe(expectedUser);
    }));

    it('Should return an empty object when trying to get current user if not authenticated', inject(function ($state, authService) {
        // Given
        spyOn($state, 'transitionTo');
        spyOn(authService, 'isAuthenticated').andReturn(false);

        // When
        var currentUser = scope.loggedInUser();

        // Then
        expect(currentUser).toEqual({});
    }));

    it('Should have user not logged', inject(function ($state) {
        // Given
        spyOn($state, 'transitionTo');
        // no user interaction was done before

        // Then
        expect(scope.isLogged()).toBe(false);
    }));

    it('Should have user logged in', inject(function ($state) {
        // Given
        spyOn($state, 'transitionTo');
        spyOn(authSrv, 'isAuthenticated').andReturn(true);

        // Then
        expect(scope.isLogged()).toBe(true);
    }));

    it ('Should clear password after receiving error response from server', inject(function ($state, $q) {
        // Given
        var defer = $q.defer();
        defer.reject({status: 401});
        spyOn(authSrv, 'login').andReturn(defer.promise);
        spyOn($state, 'transitionTo');

        // When
        scope.user.login = 'login';
        scope.user.password = 'passwordValue';
        scope.login();
        scope.$apply();

        // Then
        expect(scope.user.password).toBe('');
        expect(scope.user.login).toBe('login')
    }));

    it ('Should clear login and password after receiving success response from server', inject(function ($state, $q) {
        // Given
        var defer = $q.defer();
        defer.resolve();
        spyOn(authSrv, 'login').andReturn(defer.promise);
        spyOn($state, 'transitionTo');

        // When
        scope.user.login = 'login';
        scope.user.password = 'passwordValue';
        scope.login();
        scope.$apply();

        // Then
        expect(scope.user.password).toBe('');
        expect(scope.user.login).toBe('');
    }));


    it('Calling logout should log out from angular layer', inject(function ($state) {
        // Given
        spyOn($state, 'transitionTo');
        spyOn(authSrv, 'logout').andReturn(q.when('anything'));


        // When
        scope.logout();

        // Then
        expect(authSrv.logout).toHaveBeenCalled();
    }));
});
