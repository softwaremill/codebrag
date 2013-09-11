angular.module('codebrag.common.directives')

    .directive('activeForStates', function ($state) {
        var state = $state;
        return {
            restrict: 'A',
            link: function (scope, elem, attrs) {

                var activeStates = attrs['activeForStates'].split(',');
                hookOnEvent();
                markActive();

                function markActive() {
                    var stateName = state.$current.name;
                    _.contains(activeStates, stateName) ? elem.addClass("active") : elem.removeClass("active");
                }

                function hookOnEvent() {
                    scope.$on('$stateChangeSuccess', function () {
                        markActive();
                    });
                }
            }
        };
    });

