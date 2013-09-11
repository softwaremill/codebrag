angular.module('codebrag.commits').directive('commitMsgHeadline', function() {
    return {
        restrict: 'E',
        scope: {
            message: '='
        },
        replace: true,
        template: '<span ng-bind-html-unsafe="messageHeadline"></span>',
        link: function(scope, el, attrs) {
            var removeWatcher = scope.$watch('message', function(val) {
                if(val) {
                    scope.messageHeadline = val.split(/\n+/)[0] || '(no headline for this commit)';
                    removeWatcher();
                }
            });
        }
    };
});