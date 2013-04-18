angular.module('codebrag.common.directives')

    // it prevents event bubbling
    // useful for nested click events e.g. ng-click within ng-click

    .directive('isolateClick', function() {
        return function(scope, element) {
            $(element).click(function(event) {
                event.stopPropagation();
            });
        }
    });