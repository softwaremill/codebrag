/* global document */
angular.module('codebrag.common.directives')

    // quick and dirty implementation of scrolling to id
    // with element highlight - will probably be changed together with new UI

    .directive('scrollable', function(events) {

        var maxTimeoutCount = 50;

        var pollingInterval = 100;

        var scrollableReactionAttr = 'data-scrollable-reaction';

        var glowTargetReactionClass = 'scroll-target-reaction-mark';
        var removeGlowAnimationClass = 'scroll-target-reaction-done';

        function findElementToMarkAsActive(baseElement) {
            if(baseElement.attr(scrollableReactionAttr)) {
                return baseElement;
            }
            return baseElement.closest('[' + scrollableReactionAttr + ']');
        }

        return {
            restrict: 'A',
            link: function(scope, elem, attrs) {

                var timeoutsCount = 0;

                var scrollToId = scope.$eval(attrs.scrollable);
                function scrollIfElementPresent() {
                    timeoutsCount++;
                    var element = document.getElementById(scrollToId);
                    if(element) {
                        var $el = findElementToMarkAsActive($(element));
                        $el.addClass(glowTargetReactionClass);
                        var options = {
                            duration: 800,
                            offset: -400,
                            easing:'easeInOutExpo',
                            onAfter: function() {
                                $el.addClass(removeGlowAnimationClass);
                            }
                        };
                        $('.diff-wrapper').scrollTo('#' + scrollToId, options);
                    } else {
                        timeoutsCount < maxTimeoutCount && setTimeout(scrollIfElementPresent, pollingInterval);
                    }
                }
                setTimeout(scrollIfElementPresent, pollingInterval);
                scope.$on(events.scrollOnly, function() {
                    scrollIfElementPresent();
                });
            }
        };
    });

