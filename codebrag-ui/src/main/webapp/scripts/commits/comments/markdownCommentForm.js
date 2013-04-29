angular.module('codebrag.commits.comments')

    .directive('markdownCommentForm', function($q) {
        var mdConverter = Markdown.getSanitizingConverter();
        var additionalArgsName = 'extraArgs';
        return {
            restrict: 'E',
            scope: true,
            templateUrl: 'views/commentForm.html',
            link: function(scope, element, attrs) {

                scope.togglePreviewMode = function() {
                    scope.previewModeOn = !scope.previewModeOn;
                    if(scope.previewModeOn) {
                        scope.preview = mdConverter.makeHtml(scope.content || 'Nothing to preview');
                    }
                };

                scope.submitComment = function(content) {
                    var submitArguments = [content];
                    if(_.isString(attrs[additionalArgsName])) {
                        _.forEach(attrs[additionalArgsName].split(","), function(el) {
                            submitArguments.push(scope[el]);
                        });
                    }
                    $q.when(scope.$parent[attrs.action].apply(this, submitArguments)).then(function() {
                        scope.content = '';
                        scope.previewModeOn = false;
                    });
                }
            }
        }
    });