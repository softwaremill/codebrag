angular.module('codebrag.commits.comments')

    .directive('commentable', function($compile, events) {
        var template = $('#inlineCommentForm').html();
        return {
            restrict: 'A',
            link: function(scope, element) {
                var alreadyOpened = false;

                var onclick = function() {
                    if(alreadyOpened) {
                        _locateCommentFormRow().show();
                        return;
                    }
                    element.append(template);
                    $compile($('tr.comment-form', element).contents())(scope);
                    alreadyOpened = true;
                    scope.$digest();
                };

                var codeLine = $('td.diff-line-code', element);
                var commentBox = $('a.comment', element);
                codeLine.bind('click', onclick);
                commentBox.bind('click', onclick);

                scope.$on(events.closeForm, function(event) {
                    event.stopPropagation();
                    _locateCommentFormRow().hide();
                });

                function _locateCommentFormRow() {
                    return element.find('tr.comment-form');
                }
            }
        };
    });
