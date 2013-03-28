angular.module("codebrag.session")

    .factory('httpAuthInterceptor', function ($q, $location, $rootScope, $injector) {

        function success(response) {
            return response;
        }

        function error(response) {
            if (response.status === 401) { // user is not logged in
                var authService = $injector.get("authService")
                if (authService.isAuthenticated()) {
                    authService.logout(); // Http session expired / logged out - logout on Angular layer
                    $rootScope.$broadcast("codebrag:httpAuthError", {status: response.status, message: 'Your session timed out. Please login again.'});
                    $location.path("/login");
                }
            }
            return $q.reject(response);
        }

        return function (promise) {
            return promise.then(success, error);
        };

    });
