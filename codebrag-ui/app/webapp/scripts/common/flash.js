angular.module('codebrag.common')
    .factory('flash', function ($rootScope) {
        var queue = [], currentMessage = '';

        $rootScope.$on('$stateChangeSuccess', function() {
            if (queue.length > 0)
                currentMessage = queue.shift();
            else
                currentMessage = '';
        });

        return {
            set: function(message) {
                queue.push(message);
            },
            get: function() {
                return currentMessage;
            }
        };
    });