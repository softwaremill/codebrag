angular.module("codebrag.licence")

    .factory('httpLicenceExpirationStatusInterceptor', function ($q, $rootScope, events) {

        function success(response) {
            return response;
        }

        function shouldBeIntercepted(response) {
            return response.status === 402;
        }

        function error(response) {
            if (shouldBeIntercepted(response)) {
                $rootScope.$broadcast('codebrag:licenceExpired');
            }
            return $q.reject(response);
        }

        return function (promise) {
            return promise.then(success, error);
        };

    });
