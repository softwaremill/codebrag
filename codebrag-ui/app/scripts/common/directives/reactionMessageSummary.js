angular.module('codebrag.common.directives')

    .directive('reactionMessageSummary', function ($filter) {
        function removeAllTags(message) {
            return $(message).text();
        }

        return {
            restrict: 'E',
            template: '<span>{{ reactionMessage }}</span>',
            replace: true,
            scope: {
                reaction: '='
            },
            link: function (scope) {
                var reaction = scope.reaction;
                if (reaction.message) {
                    var formattedMessage = marked(reaction.message);
                    scope.reactionMessage = $filter('truncate')(removeAllTags(formattedMessage), 50);
                } else {
                    scope.reactionMessage = reaction.reactionAuthor + ' liked your code.';
                }
            }
        };
    });