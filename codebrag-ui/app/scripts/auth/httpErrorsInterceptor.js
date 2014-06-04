angular.module("codebrag.auth")

    .factory('httpErrorsInterceptor', function ($q, $location, $rootScope, events) {

        function success(response) {
            return response;
        }

        function shouldBeIntercepted(response) {
            var codesToIgnore = [400, 401, 402, 403];
            var bypass = codesToIgnore.filter(function(code) { return code === response.status; }).length > 0;
            return !bypass && angular.isUndefined(response.config.bypassInterceptors);
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
