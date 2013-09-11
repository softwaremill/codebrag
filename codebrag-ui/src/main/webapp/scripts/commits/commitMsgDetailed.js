angular.module('codebrag.commits').directive('commitMsgDetailed', function() {
    return {
        restrict: 'E',
        scope: {
            message: '='
        },
        replace: true,
        template: '<span ng-bind-html-unsafe="messageDetailed"></span>',
        link: function(scope, el, attrs) {
            var removeWatcherFn = scope.$watch('message', function(val) {
                if(val) {
                    var parts = val.split(/\n+/);
                    if(parts.length > 1) {
                        parts.shift();
                        scope.messageDetailed = parts.join('<br>');
                    }
                    removeWatcherFn();
                }
            });
        }
    };
});

