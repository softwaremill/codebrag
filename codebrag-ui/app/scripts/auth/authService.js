angular.module('codebrag.auth').factory('authService', function ($http, httpRequestsBuffer, $q, $rootScope, events, User) {
    'use strict';

    var currentUser = User.guest();

    function setLoggedInUser(userData) {
        currentUser.loggedInAs(userData);
        updateLoggedInUserInRootScope(currentUser, $rootScope);
        $rootScope.$broadcast(events.loggedIn);
    }

    function updateLoggedInUserInRootScope(loggedInUser, $rootScope) {
        $rootScope.loggedInUser = loggedInUser;
    }

    return {

        login: function (user) {
            var loginRequest = $http.post('rest/users', user, {bypassInterceptors: true});
            return loginRequest.then(function (response) {
                setLoggedInUser(response.data);
                httpRequestsBuffer.retryAllRequest();
                return currentUser;
            });
        },

        logout: function () {
            return $http.get('rest/users/logout');
        },

        isAuthenticated: function () {
            return currentUser.isAuthenticated();
        },

        isNotAuthenticated: function () {
            return currentUser.isGuest();
        },

        requestCurrentUser: function () {
            if (currentUser.isAuthenticated()) {
                return $q.when(currentUser);
            }
            return $http.get('rest/users').then(function (response) {
                setLoggedInUser(response.data);
                return $q.when(currentUser);
            });

        },

        isFirstRegistration: function () {
            if (currentUser.isAuthenticated()) {
                return $q.reject();
            } else {
                return $http.get('rest/users/first-registration').then(function (response) {
                    var firstRegistration = response.data.firstRegistration;
                    return $q.when(firstRegistration);
                });
            }
        },

        loggedInUser: currentUser

    };

});