angular.module('codebrag.common.directives')

	.directive('httpRequestTracker', function($http) {
		return {
			restricted: 'A',
			scope: {},			
			link: function(scope, element) {
				scope.$watch(function() {
					return $http.pendingRequests.length;
				}, function(value) {
					if(value > 0) {
						element.show();
						return;
					}
					element.hide();
				});
			}
		};
	});