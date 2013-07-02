angular.module('codebrag.common.directives')

    .directive("messagePopup", function ($rootScope, events) {

        var template = $('#errorPopup').html();

        function displayPopupHandler(scope, element, data) {

            scope.errorMsg = data;
            element.empty();
            element.append(template);
            element.fadeIn(500, function() {
                setTimeout(function () {
                    element.fadeOut('slow');
                }, 2000);
            });
        }

        return {
            restrict: "A",
            link: function (scope, element) {
                var handledEvents = [events.httpError, events.authError];
                _.forEach(handledEvents, function(event) {
                    scope.$on(event, function(event, data) {
                        displayPopupHandler(scope, element, data);
                    });
                });
            }
        };
    });