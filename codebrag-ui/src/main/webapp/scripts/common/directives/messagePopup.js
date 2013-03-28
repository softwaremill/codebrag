angular.module('codebrag.common.directives')

    .directive("messagePopup", function ($rootScope) {
        return {
            restrict: "A",
            link: function (scope, element) {
                $rootScope.$on("codebrag:httpError", function(event, data) {
                    scope.errorMsg = data;
                    element.fadeIn(500, function() {
                        setTimeout(function () {
                            element.fadeOut('slow');
                        }, 2000);
                    });
                });
            }
        };
    });