angular.module('codebrag.common.directives')

    .directive('reactionMessageSummary', function() {

        return {
            restrict: 'E',
            template: '<span>{{reactionMessage | truncate:50}}</span>',
            replace: true,
            scope: {
                reaction: '='
            },
            link: function(scope, el, attrs) {
                var reaction = scope.reaction;
                if(reaction.message) {
                    scope.reactionMessage = reaction.message;
                } else {
                    scope.reactionMessage = reaction.reactionAuthor + ' liked your code.';
                }
            }
        }
    });