angular.module("codebrag.auth")

    .factory('httpAuthInterceptor', function ($q, $rootScope, httpRequestsBuffer, events) {

        function success(response) {
            return response;
        }

        function shouldBeIntercepted(response) {
            return response.status === 401 && angular.isUndefined(response.config.bypassInterceptors);
        }

        function error(response) {
            if (shouldBeIntercepted(response)) {
                var deferred = $q.defer();
                httpRequestsBuffer.append(response.config, deferred);
                $rootScope.$broadcast(events.loginRequired);
                return deferred.promise;
            }
            return $q.reject(response);
        }

        return function (promise) {
            return promise.then(success, error);
        };

    });