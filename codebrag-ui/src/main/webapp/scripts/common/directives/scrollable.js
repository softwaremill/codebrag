angular.module('codebrag.common.directives')

    // quick and dirty implementation of scrolling to id
    // with element highlight - will probably be changed together with new UI

    .directive('scrollable', function(events) {
        var pollingInterval = 10;
        var scrollDuration = 500;
        return {
            restrict: 'A',
            link: function(scope, elem, attrs) {
                var scrollToId = scope.$eval(attrs.scrollable);
                function scrollIfElementPresent() {
                    var element = document.getElementById(scrollToId);
                    if(element) {
                        var options = {
                            duration: 800,
                            offset: -400,
                            easing:'easeInOutExpo',
                            onAfter: function() {
                                // mark target element here
                            }
                        };
                        $('.diff-wrapper').scrollTo('#' + scrollToId, options);
                    } else {
                        setTimeout(scrollIfElementPresent, pollingInterval);
                    }
                }
                setTimeout(scrollIfElementPresent, pollingInterval);
                scope.$on(events.scrollOnly, function() {
                    scrollIfElementPresent();
                });
            }
        }
    });

