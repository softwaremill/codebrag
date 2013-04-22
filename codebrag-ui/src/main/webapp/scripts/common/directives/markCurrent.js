angular.module('codebrag.common.directives')

    .directive('markCurrent', function ($stateParams) {

        return {
            restrict: 'A',
            link: function (scope, element, attributes) {

                var options = {activeClass: 'active-element', currentAtrrName: 'markCurrent'};

                markActive();
                hookOnEvent();

                function markActive() {
                    var currentValue = scope.$eval(attributes[options.currentAtrrName]);
                    if(currentValue === $stateParams.id) {
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