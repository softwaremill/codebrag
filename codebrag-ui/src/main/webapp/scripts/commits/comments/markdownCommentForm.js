angular.module('codebrag.commits.comments')

    .directive('markdownCommentForm', function($q, events, authService) {
        var mdConverter = Markdown.getSanitizingConverter();
        var additionalArgsName = 'extraArgs';
        var closeableName = 'closeable';
        return {
            restrict: 'E',
            replace: true,
            scope: true,
            templateUrl: 'views/commentForm.html',
            link: function(scope, element, attrs) {

                scope.togglePreviewMode = function() {
                    scope.previewModeOn = !scope.previewModeOn;
                    if(scope.previewModeOn) {
                        scope.preview = mdConverter.makeHtml(scope.content || 'Nothing to preview');
                    }
                };
                scope.username = authService.loggedInUser.fullName;

                scope.avatarUrl = authService.loggedInUser.avatarUrl;

                scope._submitComment = function(content) {
                    var submitArguments = [content];
                    if(_.isString(attrs[additionalArgsName])) {
                        _.forEach(attrs[additionalArgsName].split(","), function(el) {
                            submitArguments.push(scope[el]);
                        });
                    }
                    $q.when(scope[attrs.action].apply(this, submitArguments)).then(function() {
                        scope.content = '';
                        scope.previewModeOn = false;
                        scope.closeForm();
                    });
                };

                scope.isCloseable = function() {
                    return !!attrs[closeableName];
                };

                scope.closeForm = function() {
                    if(!!attrs[closeableName]) {
                        scope.$emit(events.closeForm);
                    }
                };

            }
        }
    });