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
                        _locateCommentFormRow().show();
                        return;
                    }
                    element.append(template);
                    $compile($('tr.comment-form', element).contents())(scope);
                    alreadyOpened = true;
                    scope.$digest();
                });

                scope.$on('codebrag:closeForm', function(event) {
                    event.stopPropagation();
                    _locateCommentFormRow().hide();
                });

                function _locateCommentFormRow() {
                    return element.find('tr.comment-form');
                }
            }
        }
    });
