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
            } else if (response.status === 403) {
                console.log(response.data);
                // do nothing, user is trying to modify data without privileges
            } else if (response.status === 404) {
                $location.path("/error404");
            } else if (response.status === 500) {
                $location.path("/error500");
            } else {
                $location.path("/error");
            }
            return $q.reject(response);
        }

        return function (promise) {
            return promise.then(success, error);
        };

    });
