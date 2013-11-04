angular.module('codebrag.common.directives')

    .directive('userAvatar', function (authService, events) {
        //public avatar to be used by gravatar.com (if as default avatar)
        var defaultPublicAvatar = "http://codebrag.com/stylesheets/images/avatar.png";

        var defaultAvatarUrl = 'assets/images/avatar.png';


        function buildDefaultAvatarParam() {
            return "?d=" + encodeURI(defaultPublicAvatar);
        }


        function on(scope) {
            return function () {
                return authService.requestCurrentUser().then(function (user) {
                    if (!user.settings.avatarUrl || !user.settings.avatarUrl.length) {
                        scope.avatarUrl = defaultAvatarUrl;
                    } else {
                        scope.avatarUrl = user.settings.avatarUrl + buildDefaultAvatarParam();
                    }
                });
            };
        }

        function watch(scope) {
            return function (val) {
                if (!val || !val.length) {
                    scope.avatarUrl = defaultAvatarUrl;
                } else {
                    scope.avatarUrl = val + buildDefaultAvatarParam();
                }
            };
        }

        return {
            template: '<img ng-src="{{avatarUrl}}"></img>',
            restrict: 'E',
            scope: {url: '='},
            link: function (scope, el, attrs) {
                if (attrs.loggedInUser === "true") {
                    on(scope)();
                    scope.$on(events.loggedIn, on(scope));
                } else {
                    scope.$watch('url', watch(scope));
                }
            }
        };
    });