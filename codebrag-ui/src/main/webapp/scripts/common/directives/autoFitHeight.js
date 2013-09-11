angular.module('codebrag.common.directives')

    .directive('autoFitHeight', function() {

        function HeightsResizer() {

            var windowArea = $(window);
            var headerArea = $('header');
            var sortingArea = $('.commits-tab-header');

            function recalculateHeights() {

                function getHeightOrDefault(element) {
                    return (element.length && element.outerHeight()) || 0;
                }

                return {
                    windowHeight: getHeightOrDefault(windowArea),
                    headerHeight: getHeightOrDefault(headerArea),
                    sortingHeight:  getHeightOrDefault(sortingArea)
                };
            }

            function resizeDiffHeight(diffArea, newHeights) {
                var newDiffAreaHeight = newHeights.windowHeight - newHeights.headerHeight - newHeights.sortingHeight;
                diffArea.height(newDiffAreaHeight);
            }

            function resizeListAreaHeight(listArea, newHeights) {
                var newListAreaHeight = newHeights.windowHeight - newHeights.headerHeight - newHeights.sortingHeight;
                listArea.height(newListAreaHeight);
            }

            this.resize = function(listArea, diffArea) {
                var newHeights = recalculateHeights();
                resizeListAreaHeight(listArea, newHeights);
                resizeDiffHeight(diffArea, newHeights);
            };

        }

        var listAreaSelector = '.items-container';
        var diffAreaSelector = '.diff';


        return {
            restrict: 'A',
            link: function(scope, el, attrs) {
                var resizer = new HeightsResizer();
                var listArea = el.find(listAreaSelector);
                var diffArea = el.find(diffAreaSelector);
                resizer.resize(listArea, diffArea);
                $(window).on('resize', resizeWithDelay());

                function resizeWithDelay() {
                    return _.debounce(function() {
                        resizer.resize(listArea, diffArea);
                    }, 50);
                }
            }
        };

    });

