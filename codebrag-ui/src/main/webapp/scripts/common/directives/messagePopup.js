angular.module('codebrag.common.directives')

    .directive("messagePopup", function ($rootScope, events) {

        function displayPopupHandler(scope, element, data) {
            scope.errorMsg = data;
            element.fadeIn(500, function() {
                setTimeout(function () {
                    element.fadeOut('slow');
                }, 2000);
            });
        }

        return {
            restrict: "A",
            link: function (scope, element) {
                var events = [events.httpError, events.authError];
                _.forEach(events, function(event) {
                    scope.$on(event, function(event, data) {
                        displayPopupHandler(scope, element, data);
                    });
                });
            }
        };
    });