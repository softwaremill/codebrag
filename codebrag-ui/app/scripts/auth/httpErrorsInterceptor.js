angular.module("codebrag.auth")

    .factory('httpErrorsInterceptor', function ($q, $location, $rootScope, events) {

        function success(response) {
            return response;
        }

        function shouldBeIntercepted(response) {
            return response.status !== 401 && response.status !== 403 && angular.isUndefined(response.config.bypassInterceptors);
        }

        function error(response) {
            var errorMessage = {
                status: response.status,
                text: "Something is seriously wrong, officer."
            };
            if (shouldBeIntercepted(response)) {
                $rootScope.$broadcast(events.httpError, errorMessage);
            }
            return $q.reject(response);
        }

        return function (promise) {
            return promise.then(success, error);
        };

    });
