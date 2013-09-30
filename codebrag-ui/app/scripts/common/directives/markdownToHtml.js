angular.module('codebrag.common.directives')

    .directive('markdownToHtml', function() {
        return {
            restrict: 'E',
            scope: {
                content: '='
            },
            link: function(scope, element) {
                element.html(marked(scope.content || ''));
            }
        };
    });
