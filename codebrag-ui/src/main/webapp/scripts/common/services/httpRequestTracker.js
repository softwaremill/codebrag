angular.module('codebrag.common.services')

    .factory('httpRequestTracker', function($http){
        return {
            hasPendingRequests: function() {
                return $http.pendingRequests.length > 0;
            }
        }
    });