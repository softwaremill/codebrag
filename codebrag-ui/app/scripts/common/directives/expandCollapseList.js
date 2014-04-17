angular.module('codebrag.common.directives')

    .directive('expandCollapseList', function($window, events) {
        function triggerWindowResizeToLetCommentsFormAutoFit() {
            $($window).resize();
        }

        function expand(el) {
            var commitsList = $('.commits');
            var diffArea = el.closest('.diff');
            diffArea.removeClass('opened');
            commitsList.animate({opacity: 1}, 500);
            el.find('i').removeClass().addClass('icon-chevron-left');
            triggerWindowResizeToLetCommentsFormAutoFit();
        }

        return {
            restrict: 'A',
            link: function(scope, el) {
                el.on('click', function() {
                    var diffArea = el.closest('.diff');
                    var commitsList = $('.commits');
                    diffArea.toggleClass('opened');
                    if (diffArea.hasClass('opened')) {
                        commitsList.animate({opacity: 0}, 500);
                        setTimeout(function() {
                            triggerWindowResizeToLetCommentsFormAutoFit();
                            el.find('i').removeClass().addClass('icon-chevron-right');
                        }, 1000);
                    } else {
                        expand(el);
                    }
                });

                scope.$on(events.commitsTabOpened, function() {
                    expand(el);
                });
                scope.$on(events.followupsTabOpened, function() {
                    expand(el);
                });
                scope.$on(events.branches.branchChanged, function() {
                    expand(el);
                })
            }
        };
    });