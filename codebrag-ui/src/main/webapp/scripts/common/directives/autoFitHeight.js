angular.module('codebrag.common.directives')

    .directive('autoFitHeight', function() {

        function HeightsResizer() {

            var windowArea = $(window);
            var headerArea = $('header');
            var sortingArea = $('.sorting');
            var loadMoreArea = $('.commits-count');

            function recalculateHeights() {
                return {
                    windowHeight: windowArea.height(),
                    headerHeight: headerArea.outerHeight(),
                    sortingHeight:  sortingArea.outerHeight(),
                    loadMoreHeight: loadMoreArea.outerHeight()
                }
            }

            function resizeDiffHeight(diffArea, newHeights) {
                var diffTopPadding = parseInt(diffArea.css('padding-top'), 10);
                var newDiffAreaHeight = newHeights.windowHeight - newHeights.headerHeight - 2 * diffTopPadding;
                diffArea.height(newDiffAreaHeight);
            }

            function resizeListAreaHeight(listArea, newHeights) {
                var newListAreaHeight = newHeights.windowHeight - newHeights.headerHeight - newHeights.sortingHeight - newHeights.loadMoreHeight;
                listArea.height(newListAreaHeight)
            }

            this.resize = function(listArea, diffArea) {
                var newHeights = recalculateHeights();
                resizeListAreaHeight(listArea, newHeights);
                resizeDiffHeight(diffArea, newHeights);
            }

        }

        var listAreaSelector = '.items-container';
        var diffAreaSelector = '.diff';

        var resizer = new HeightsResizer();

        return {
            restrict: 'A',
            link: function(scope, el, attrs) {
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
        }

    });

