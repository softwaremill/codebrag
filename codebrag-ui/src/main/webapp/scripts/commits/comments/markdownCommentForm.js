angular.module('codebrag.commits.comments')

    .directive('markdownCommentForm', function() {
        var mdConverter = Markdown.getSanitizingConverter();
        return {
            restrict: 'E',
            controller: function($scope) {
                $scope.togglePreviewMode = function() {
                    $scope.previewModeOn = !$scope.previewModeOn;
                    if($scope.previewModeOn) {
                        $scope.preview = mdConverter.makeHtml($scope.body || 'Nothing to preview');
                    }
                };

                $scope.$on('codebrag:commentCreated', function() {
                    $scope.body = '';
                    $scope.previewModeOn = false;
                });
            },
            scope: {
                submit: '&'
            },
            templateUrl: 'views/commentForm.html'
        }
    });
