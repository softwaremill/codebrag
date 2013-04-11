angular.module('codebrag.auth')

    .factory('authService', function($http, httpRequestsBuffer, $q) {

        var authService = {

            loggedInUser: undefined,

            login: function(user) {
                var loginRequest = $http.post('rest/users', user, {bypassQueue: true});
                return loginRequest.then(function(response) {
                    authService.loggedInUser = response.data;
                    httpRequestsBuffer.retryAllRequest();
                });
            },

            logout: function() {
                var logoutRequest = $http.get('rest/users/logout');
                return logoutRequest.then(function() {
                    authService.loggedInUser = undefined;
                })
            },

            isAuthenticated: function() {
                return !angular.isUndefined(authService.loggedInUser);
            },

            isNotAuthenticated: function() {
                return !authService.isAuthenticated();
            },

            requestCurrentUser: function() {
                if (authService.isAuthenticated()) {
                    return $q.when(authService.loggedInUser);
                }
                return $http.get('rest/users').then(function(response) {
                    authService.loggedInUser = response.data;
                    return $q.when(authService.loggedInUser);
                });
            }

        };

        return authService;

    });