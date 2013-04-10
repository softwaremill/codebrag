angular.module("codebrag.session")

    .factory('httpAuthInterceptor', function ($q, $location, $rootScope, $injector) {

        function success(response) {
            return response;
        }

        function error(response) {
            var eventData = {};
            if (response.status === 401) { // user is not logged in
                eventData.status = response.status;
                var authService = $injector.get("authService")
                if (authService.isAuthenticated()) {
                    authService.logout(); // Http session expired / logged out - logout on Angular layer
                    eventData.text = 'Your session timed out. Please login again.'
                } else {
                    eventData.text = 'Login required.'
                }
                $rootScope.$broadcast("codebrag:httpAuthError", eventData);
            }
            return $q.reject(response);
        }

        return function (promise) {
            return promise.then(success, error);
        };

    });
