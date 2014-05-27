 'use strict';

describe("Session Controller", function () {

    var scope, $httpBackend, ctrl, authService, $q, $rootScope, $window;

    beforeEach(module(function($provide) {
        $provide.value('$window', $window = angular.mock.createMockWindow());
    }));

    beforeEach(module('codebrag.session'));

    beforeEach(inject(function (_$httpBackend_, _$rootScope_, $routeParams, configService, $controller, _authService_, _$q_) {
        $httpBackend = _$httpBackend_;
        $httpBackend.expect("GET", "rest/config/").respond({demo: true});

        $rootScope = _$rootScope_;
        scope = $rootScope.$new();
        authService = _authService_;
        ctrl = $controller('SessionCtrl', {$scope: scope, authService: authService, flash: {}});
        $q = _$q_;

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
        spyOn(authService, 'login').andReturn($q.when('anything'));
        spyOn($state, 'transitionTo');

        // When
        scope.login();

        // Then
        expect(authService.login).toHaveBeenCalled();
    }));

    it('Should not call login rest service when form is invalid', inject(function ($state) {
        // Given
        scope.loginForm.$invalid = true;
        spyOn($state, 'transitionTo');
        spyOn(authService, 'login');

        // When
        scope.login();

        // Then
        expect(authService.login).not.toHaveBeenCalled();

    }));

    it('should keep current user updated', inject(function ($state, authService) {
        // Given
        spyOn($state, 'transitionTo');
        expect(scope.loggedInUser.isGuest()).toBe(true);
        expect(scope.loggedInUser.isAuthenticated()).toBe(false);

        // When
        authService.loggedInUser.loggedInAs({fullName: 'John Doe'});

        // Then
        expect(scope.loggedInUser).toBe(authService.loggedInUser);
        expect(scope.loggedInUser.isGuest()).toBe(false);
        expect(scope.loggedInUser.isAuthenticated()).toBe(true);
    }));

    it('Should return guest user when trying to get current user if not authenticated', inject(function ($state, authService) {
        // Given
        spyOn($state, 'transitionTo');

        // When
        var currentUser = scope.loggedInUser;

        // Then
        expect(currentUser.isGuest()).toEqual(true);
    }));

    it('Should clear password after receiving error response from server', inject(function ($state) {
        // Given
        var defer = $q.defer();
        defer.reject({status: 401});
        spyOn(authService, 'login').andReturn(defer.promise);
        spyOn($state, 'transitionTo');

        // When
        scope.user.login = 'login';
        scope.user.password = 'passwordValue';
        scope.login();
        scope.$apply();

        // Then
        expect(scope.user.password).toBe('');
        expect(scope.user.login).toBe('login');
    }));

    it('Should clear login and password after receiving success response from server', inject(function ($state) {
        // Given
        var defer = $q.defer();
        defer.resolve();
        spyOn(authService, 'login').andReturn(defer.promise);
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


    it('Calling logout should log out and redirect to /', inject(function ($state, events, $window) {
        // Given
        spyOn(authService, 'logout').andReturn($q.when());

        // When
        scope.logout();
        scope.$digest();

        // Then
        expect(authService.logout).toHaveBeenCalled();
        expect($window.location).toBe('/');
    }));

});
