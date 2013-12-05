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
                // added some code to debug an issue with scrolling
                scope.$watch(function() {
                    return $('#diff-area').height();
                }, function(height) {
                    console.log('Diff height is', height);
                });

                var timeoutsCount = 0;

                var scrollToId = scope.$eval(attrs.scrollable);

                function scrollTo(domElement) {
                    var $el = findElementToMarkAsActive($(domElement));
                    $el.addClass(glowTargetReactionClass);
                    var options = {
                        duration: 800,
                        offset: -400,
                        easing: 'easeInOutExpo',
                        onAfter: function () {
                            $el.addClass(removeGlowAnimationClass);
                        }
                    };
                    // added some code to debug an issue with scrolling
                    console.log('Diff height when scrolling ', $('#diff-area').height());
                    $('.diff-wrapper').scrollTo('#' + scrollToId, options);
                }

                function scrollIfElementPresent() {
                    timeoutsCount++;
                    var element = document.getElementById(scrollToId);
                    if(element) {
                        console.log('Scrollable target found');
                        // queue scroll to let DOM stabilize
                        setTimeout(function() {
                            scrollTo(element);
                        }, pollingInterval);
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

