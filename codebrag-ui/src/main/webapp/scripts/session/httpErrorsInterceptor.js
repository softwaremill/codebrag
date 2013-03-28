angular.module("codebrag.session")

    .factory('httpErrorsInterceptor', function ($q, $location, $rootScope) {

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
                $rootScope.$broadcast("codebrag:httpError", errorMessage);
            }
            return $q.reject(response);
        }

        return function (promise) {
            return promise.then(success, error);
        };

    });
