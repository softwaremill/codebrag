angular.module('codebrag.tour')

    .directive('pageTour', function() {
        return {
            restrict: 'E',
            templateUrl: 'views/tour/pageTour.html'
        }
    })

    .directive('pageTourPopup', function() {

        return {
            restrict: 'E',
            transclude: true,
            templateUrl: 'views/tour/pageTourPopup.html',
            scope: {
                dismiss: '&',
                visibleIf: '=',
                positionCssClass: '@',
                arrowCssClass: '@',
                customButtons: '='
            }
        }

    });
