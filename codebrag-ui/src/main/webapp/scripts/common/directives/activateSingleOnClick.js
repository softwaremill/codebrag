angular.module('codebrag.common.directives')

    .directive('activateSingleOnClick', function() {
        var addClassAttribute = "toggleClass";
        return {
            restrict: 'A',
            link: function(scope, el, attrs) {
                el.on('click', attrs.activateSingleOnClick, function(event) {
                    el.find(attrs.activateSingleOnClick).toggleClass(attrs[addClassAttribute]);
                });
            }
        }
    });
