angular.module('codebrag.commits.comments')

    .directive('commentable', function($compile) {
        var template = $('#inlineCommentForm').html();
        return {
            restrict: 'A',
            link: function(scope, element, attrs) {
                var alreadyOpened = false;
                var codeLine = $('tr:first-child', element);
                codeLine.bind('click', function() {
                    if(alreadyOpened) {
                        return;
                    }
                    element.append(template);
                    $compile($('tr.comment-form', element).contents())(scope);
                    alreadyOpened = true;
                    scope.$digest();
                });
            }
        }
    });
