angular.module("codebrag.auth")

    .directive("loginForm", function (authService, events) {

        return {
            restrict: "E",
            templateUrl: 'views/login.html',
            scope: {},
            link: function (scope) {
                scope.$watch(ifLoginRequired, function(newValue) {
                    scope.shouldDisplayLogin = newValue;
                });
                scope.$on(events.loginRequired, function() {
                    scope.shouldDisplayLogin = true;
                });
                function ifLoginRequired() {
                    return authService.isNotAuthenticated();
                }
            }
        };
    });