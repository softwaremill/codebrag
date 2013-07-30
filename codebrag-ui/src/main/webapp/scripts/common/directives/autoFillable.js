/**
 * Directive that solves an issue with browsers when browser has form fields stored and fills them on page load
 * Some browsers don't emit change event in those cases so Angular can't update model accordingly.
 * This directive sets short timeout and reapplies values from form to model.
 */

angular.module('codebrag.common.directives')

    .directive('autoFillable', function($timeout) {
        return {
            require: '?ngModel',
            restrict: 'A',
            link: function(scope, element, attrs, ngModel) {
                $timeout(function() {
                    if (ngModel.$viewValue !== element.val()) {
                        ngModel.$setViewValue(element.val());
                    }
                }, 500);
            }
        };
    });