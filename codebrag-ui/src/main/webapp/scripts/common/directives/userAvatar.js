angular.module('codebrag.common.directives')

    .directive('userAvatar', function() {
        return {
            template: '<img ng-src="{{avatarUrl}}"></img>',
            restrict: 'E',
            scope: true,
            link: function(scope, el, attrs) {
                scope.$watch(attrs.url, function(val, old) {
                    if(!val || !val.length) {
                        scope.avatarUrl = '/images/avatar.png';
                    } else {
                        scope.avatarUrl = val;
                    }
                });
            }
        }
    })

    .directive('loggedInUserAvatar', function(authService, events) {

        return {
            template: '<img ng-src="{{avatarUrl}}"></img>',
            restrict: 'E',
            scope: {},
            link: function(scope, el, attrs) {
                scope.$on(events.loggedIn, function() {
                    authService.requestCurrentUser().then(function(user) {
                        if(!user.avatarUrl || !user.avatarUrl.length) {
                            scope.avatarUrl = '/images/avatar.png';
                        } else {
                            scope.avatarUrl = user.avatarUrl;
                        }
                    });
                })
            }
        }
    });
