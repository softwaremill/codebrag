"use strict";

angular.module('smlCodebrag.directives').directive('bsBlur', function() {
    return function(scope, element, attrs) {
        element.bind("blur", function() {
            scope.$eval(attrs.bsBlur);
        });
    }
});