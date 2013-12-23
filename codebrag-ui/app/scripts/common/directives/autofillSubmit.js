/**
 * Solves an issue with browsers when browser has form fields stored and fills them on page load
 * Browsers don't emit any event when autofilling values so Angular can't update model accordingly.
 * This directive updates model values before invoking submit action.
 *
 * Input fields that should have their values rewritten should have "data-af-enabled" attribute
 */

angular.module('codebrag.common.directives')

    .directive('afSubmit', function() {
        return {
            restrict: 'A',
            link: function(scope, el, attrs) {
                el.bind('submit', function(e) {
                    e.preventDefault();
                    var underlyingElement = el[0];
                    collectMatchingInputs(underlyingElement).forEach(updateInputBinding);
                    scope.$apply(attrs.afSubmit);
                });
            }
        };

        function collectMatchingInputs(formElement) {
            var matchingSelector = '[data-af-enabled]';
            var inputs = formElement.querySelectorAll(matchingSelector);
            return Array.prototype.slice.call(inputs);
        }

        function updateInputBinding(input) {
            var $input = angular.element(input);
            var inputModelController = $input.controller('ngModel');
            if(inputModelController) {
                var value = input.value;
                if(value && value.length) {
                    inputModelController.$setViewValue(value);
                }
            }
        }
    });