angular.module('codebrag.common.directives')

    .directive('expandCollapseList', function($window) {
        function triggerWindowResizeToLetCommentsFormAutoFit() {
            $($window).resize();
        }
        return {
            restrict: 'A',
            link: function(scope, el, attrs) {
                el.on('click', function() {
                    var diffArea = el.closest('.diff');
                    var commitsList = $('.commits');
                    diffArea.toggleClass('opened');
                    if (diffArea.hasClass('opened')) {
                        commitsList.animate({opacity: 0}, 500);
                        setTimeout(function() {
                            triggerWindowResizeToLetCommentsFormAutoFit();
                            el.find('i').removeClass().addClass('icon-chevron-right')
                        }, 1000);
                    } else {
                        commitsList.animate({opacity: 1}, 500);
                        el.find('i').removeClass().addClass('icon-chevron-left');
                        triggerWindowResizeToLetCommentsFormAutoFit();
                    }

                })
            }
        }
    });