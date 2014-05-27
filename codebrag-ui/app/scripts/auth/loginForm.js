angular.module("codebrag.auth")

    .directive("loginForm", function (authService, events, $state, flash) {

        return {
            restrict: "E",
            templateUrl: 'views/login.html',
            scope: {},
            link: function (scope) {
                function updateShouldDisplayLogin() {
                    var noLogin = $state.current.noLogin;
                    scope.shouldDisplayLogin = scope.loginRequired && (!noLogin);
                }
                scope.$watch(ifLoginRequired, function(newValue) {
                    scope.loginRequired = newValue;
                    updateShouldDisplayLogin();
                });
                scope.$on(events.loginRequired, function() {
                    scope.loginRequired = true;
                    updateShouldDisplayLogin();
                });
                scope.$on("$stateChangeSuccess", function() {
                    updateShouldDisplayLogin();
                    scope.registrationSuccess = flash.get().length > 0;
                    scope.registrationSuccessMessage = flash.get();
                });
                function ifLoginRequired() {
                    return authService.loggedInUser.isGuest();
                }
            }
        };
    });