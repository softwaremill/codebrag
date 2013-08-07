angular.module('codebrag.common.directives')

    .directive('reactionMessageSummary', function($filter) {

        return {
            restrict: 'E',
            template: '<span ng-bind-html-unsafe="reactionMessage"></span>',
            replace: true,
            scope: {
                reaction: '='
            },
            link: function(scope, el, attrs) {
                var reaction = scope.reaction;
                if(reaction.message) {
                    scope.reactionMessage = $filter('truncate')(reaction.message, 50);
                } else {
                    scope.reactionMessage = reaction.reactionAuthor + ' liked your code.';
                }
            }
        }
    });