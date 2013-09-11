angular.module('codebrag.common.directives')

    .directive('markdownToHtml', function() {
        var converter = Markdown.getSanitizingConverter();
        return {
            restrict: 'E',
            scope: {
                content: '='
            },
            link: function(scope, element) {
                element.html(converter.makeHtml(scope.content || ''));
            }
        };
    });

