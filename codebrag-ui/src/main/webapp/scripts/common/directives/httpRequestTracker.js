angular.module('codebrag.common.directives')

	.directive('httpRequestTracker', function($http) {
		return {
			restricted: 'A',
			scope: {},
			link: function(scope, element, attrs) {
                var requestType = attrs.httpRequestTracker;

                function observedRequestsCount() {
                    var pendingRequests = $http.pendingRequests;
                    if(requestType) {
                        return pendingRequests.filter(function(request) {
                            return request.requestType === requestType;
                        }).length;
                    } else {
                        return pendingRequests.length;
                    }
                }

                scope.$watch(function() {
                    return $http.pendingRequests.length
                }, function() {
                    if(observedRequestsCount()) {
                        element.show();
                        return;
                    }
                    element.fadeOut();
                });
			}
		};
	});