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
                        var $el = $(element);
                        $el.parent('div').addClass('scrolled-to');
                        $('html, body').animate({
                            scrollTop: $el.offset().top - 80
                        }, scrollDuration, function() {
                            $el.parent('div').addClass('scroll-to');
                        });
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

