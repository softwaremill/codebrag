angular.module('codebrag.session')

    .factory('authService', function($http, $q, $cookies) {

        var authService = {

            loggedInUser: null,

            login: function(user) {
                var loginRequest = $http.post('/rest/users', user);
                return loginRequest.then(function(response) {
                    authService.loggedInUser = response.data;
                });
            },

            logout: function() {
                var logoutRequest = $http.get('/rest/users/logout');
                return logoutRequest.then(function() {
                    authService.loggedInUser = null;
                })
            },

            isAuthenticated: function() {
                return !!authService.loggedInUser && $cookies["scentry.auth.default.user"];
            },

            isNotAuthenticated: function() {
                return !authService.isAuthenticated();
            },

            requestCurrentUser: function() {
                if (authService.isAuthenticated()) {
                    return $q.when(authService.loggedInUser);
                } else {
                    return $http.get('/rest/users').then(function(response) {
                        authService.loggedInUser = response.data;
                        return authService.loggedInUser;
                    });
                }
            }

        };

        return authService;

    });