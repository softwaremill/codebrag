angular.module('codebrag.commits.comments')

    .directive('commentable', function($compile) {
        var template = $('#inlineCommentForm').html();
        return {
            restrict: 'A',
            link: function(scope, element, attrs) {
                var alreadyOpened = false;
                var codeLine = $('a.comment', element);
                codeLine.bind('click', function() {
                    if(alreadyOpened) {
                        return;
                    }
                    element.append(template);
                    console.log(element.html());
                    $compile($('tr.comment-form', element).contents())(scope);
                    alreadyOpened = true;
                    scope.$digest();
                });
            }
        }
    });
