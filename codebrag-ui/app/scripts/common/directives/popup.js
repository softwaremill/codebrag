angular.module('codebrag.common.directives')

    .directive('popup', function($compile) {

        var template = '<ng-include src="templateUrl" ng-show="visible"></ng-include>';

        return {
            restrict: 'E',
            replace: false,
            scope: {
                templateUrl: '@'
            },
            link: function(scope, el, attrs) {

                var openEventName = attrs.openOn;

                scope.$on(openEventName, function () {
                    el.html(template);
                    $compile(el.find('ng-include'))(scope);
                    scope.visible = true;
                    scope.templateUrl;
                });

                scope.closePopup = function () {
                    el.html('');
                    scope.visible = false;
                };

            }
        };

    });
