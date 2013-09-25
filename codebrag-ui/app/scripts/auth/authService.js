angular.module('codebrag.auth')

    .factory('authService', function ($http, httpRequestsBuffer, $q, $rootScope, events) {

        var authService = {

            loggedInUser: undefined,

            login: function (user) {
                var loginRequest = $http.post('rest/users', user, {bypassInterceptors: true});
                return loginRequest.then(function (response) {
                    authService.loggedInUser = response.data;
                    $rootScope.$broadcast(events.loggedIn);
                    httpRequestsBuffer.retryAllRequest();
                });
            },

            logout: function () {
                var logoutRequest = $http.get('rest/users/logout');
                return logoutRequest.then(function () {
                    authService.loggedInUser = undefined;
                    $rootScope.$broadcast(events.loginRequired);
                });
            },

            isAuthenticated: function () {
                return !angular.isUndefined(authService.loggedInUser);
            },

            isNotAuthenticated: function () {
                return !authService.isAuthenticated();
            },

            requestCurrentUser: function () {
                if (authService.isAuthenticated()) {
                    return $q.when(authService.loggedInUser);
                }
                function logInIfNotYetLoggedIn(currentUser) {
                    if(authService.isNotAuthenticated()) {
                        authService.loggedInUser = currentUser;
                        $rootScope.$broadcast(events.loggedIn);
                    }
                }
                return $http.get('rest/users').then(function (response) {
                    logInIfNotYetLoggedIn(response.data);
                    return $q.when(authService.loggedInUser);
                });

            },

            isFirstRegistration: function () {
                if (authService.isAuthenticated()) {
                    return $q.when(true);
                } else {
                    return $http.get('rest/users/first-registration').then(function (response) {
                        var firstRegistration = response.data.firstRegistration;
                        return $q.when(firstRegistration);
                    });
                }
            }

        };

        return authService;

    });