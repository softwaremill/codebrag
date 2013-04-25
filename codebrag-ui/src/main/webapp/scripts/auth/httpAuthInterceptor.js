angular.module("codebrag.auth")

    .factory('httpAuthInterceptor', function ($q, $rootScope, httpRequestsBuffer) {

        function success(response) {
            return response;
        }

        function error(response) {
            if (response.status === 401 && angular.isUndefined(response.config.bypassQueue)) {
                var deferred = $q.defer();
                httpRequestsBuffer.append(response.config, deferred);
                $rootScope.$broadcast("codebrag:loginRequired");
                return deferred.promise;
            }
            return $q.reject(response);
        }

        return function (promise) {
            return promise.then(success, error);
        };

    });