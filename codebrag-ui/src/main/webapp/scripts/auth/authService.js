angular.module('codebrag.auth')

    .factory('authService', function($http, httpRequestsBuffer, $q, $rootScope, events) {

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
                var promise = $http.get('rest/users');
                return promise.then(function(response) {
                    authService.loggedInUser = response.data;
                    $rootScope.$broadcast(events.loggedIn);
                    return $q.when(authService.loggedInUser);
                });
            }

        };

        return authService;

    });