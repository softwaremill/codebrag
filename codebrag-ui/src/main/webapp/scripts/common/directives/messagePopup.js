angular.module('codebrag.common.directives')

    .directive("messagePopup", function ($rootScope) {

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
                var events = ['codebrag:httpError', 'codebrag:httpAuthError'];
                _.forEach(events, function(event) {
                    scope.$on(event, function(event, data) {
                        displayPopupHandler(scope, element, data);
                    });
                });
            }
        };
    });