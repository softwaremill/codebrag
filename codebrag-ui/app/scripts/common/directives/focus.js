
angular.module('codebrag.common.directives')
    .directive('setFocus', function(){
        return function(scope, element){
            element[0].focus();
        };
    });