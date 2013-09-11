angular.module("codebrag.auth")

    .factory('httpRequestsBuffer', function($injector) {
        var buffer = [];
        var $http;

        function retryHttpRequest(config, deferred) {
            function successCallback(response) {
                deferred.resolve(response);
            }
            function errorCallback(response) {
                deferred.reject(response);
            }
            $http = $http || $injector.get('$http');
            $http(config).then(successCallback, errorCallback);
        }

        return {
            append: function(config, deferred) {
                buffer.push({
                    config: config,
                    deferred: deferred
                });
            },
            retryAllRequest: function() {
                for (var i = 0; i < buffer.length; ++i) {
                    retryHttpRequest(buffer[i].config, buffer[i].deferred);
                }
                buffer = [];
            }
        };
    });
