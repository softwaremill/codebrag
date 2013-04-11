angular.module("codebrag.auth")

    .directive("loginForm", function (authService) {

        return {
            restrict: "E",
            templateUrl: 'views/loginPopup.html',
            scope: {},
            link: function (scope) {
                scope.$watch(ifLoginRequired, function(newValue) {
                    scope.shouldDisplayLogin = newValue;
                });
                function ifLoginRequired() {
                    return authService.isNotAuthenticated();
                }
            }
        };
    });