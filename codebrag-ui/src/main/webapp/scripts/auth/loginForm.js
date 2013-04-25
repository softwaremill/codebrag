angular.module("codebrag.auth")

    .directive("loginForm", function (authService) {

        return {
            restrict: "E",
            templateUrl: 'views/login.html',
            scope: {},
            link: function (scope) {
                scope.$watch(ifLoginRequired, function(newValue) {
                    scope.shouldDisplayLogin = newValue;
                });
                scope.$on('codebrag:loginRequired', function() {
                    scope.shouldDisplayLogin = true;
                });
                function ifLoginRequired() {
                    return authService.isNotAuthenticated();
                }
            }
        };
    });