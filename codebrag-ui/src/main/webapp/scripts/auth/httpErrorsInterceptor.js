angular.module("codebrag.auth")

    .factory('httpErrorsInterceptor', function ($q, $location, $rootScope, events) {

        function success(response) {
            return response;
        }

        function error(response) {
            var errorMessage = {
                status: response.status,
                text: "Something is seriously wrong, officer."
            }
            if (response.status !== 401) {
                console.log("Got this response:", response);
                $rootScope.$broadcast(events.httpError, errorMessage);
            }
            return $q.reject(response);
        }

        return function (promise) {
            return promise.then(success, error);
        };

    });
