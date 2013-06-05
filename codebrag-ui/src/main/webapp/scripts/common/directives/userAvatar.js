angular.module('codebrag.common.directives')

    .directive('userAvatar', function() {
        return {
            template: '<img src="{{avatarUrl}}"></img>',
            restrict: 'E',
            scope: {
                url: '='
            },
            link: function(scope, el, attrs) {
                if(!scope.url || !scope.url.length) {
                    scope.avatarUrl = '/images/avatar.png';
                } else {
                    scope.avatarUrl = scope.url;
                }
            }
        }
    });
