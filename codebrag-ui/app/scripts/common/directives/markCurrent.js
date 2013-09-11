angular.module('codebrag.common.directives')
    .directive('markCurrent', function ($stateParams) {

        return {
            restrict: 'A',
            link: function (scope, element, attributes) {

                var options = {activeClass: 'active', currentAttrName: 'markCurrent', stateParamName: 'stateParamName'};

                markActive();
                hookOnEvent();

                function markActive() {
                    var stateParamName = attributes[options.stateParamName];
                    var currentValue = scope.$eval(attributes[options.currentAttrName]);
                    if (currentValue === $stateParams[stateParamName]) {
                        element.addClass(options.activeClass);
                    } else {
                        element.removeClass(options.activeClass);
                    }
                }

                function hookOnEvent() {
                    scope.$on('$stateChangeSuccess', function() {
                        markActive();
                    });
                }

            }
        };
    });