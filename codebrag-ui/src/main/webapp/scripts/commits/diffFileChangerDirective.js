angular.module('codebrag.commits')

    .directive('diffFileChooser', function (currentCommit, $rootScope, events) {

        function fillSelectWithFilenames(scope, select) {

            function appendSingleOption(file) {
                var filePathParts = file.split(/\//);
                var fileName = filePathParts.pop();
                var dirs = filePathParts.length ? filePathParts.join('/') : './';
                select.append(new Option(fileName + ' in ' + dirs, file));
            }

            var commitAvailable = function () {
                return currentCommit.get();
            };

            scope.$watch(commitAvailable, function (commit) {
                select.empty();
                commit && commit.diffFileNames().forEach(function (file) {
                    appendSingleOption(file);
                });
            });

        }

        return {
            restrict: 'E',
            scope: {},
            template: '<select class="file-changer"></select>',
            link: function (scope, el) {
                var select = el.find('select');

                fillSelectWithFilenames(scope, select);

                scope.$on(events.diffScrolledWithFileChange, function (event, data) {
                    select.val(data.filename);
                });

                select.on('change', function() {
                    var val = $(this).val();
                    $rootScope.$broadcast(events.diffFileSelected, {filename: val});
                });

            }
        }
    })

    .directive('diffScrollSpy', function ($rootScope, events) {

        var scrollTargetSelector = 'table[data-file-name]';
        var containerSelector = '.diff-wrapper';

        function collectScrollTargets(rootContainer) {
            var scrollTargets = [];
            var pixelsAlreadyScrolled = rootContainer.scrollTop();
            rootContainer.find(scrollTargetSelector).each(function () {
                var $el = $(this);
                var targetData = {
                    filename: $el.data('file-name'),
                    offset: $el.offset().top + pixelsAlreadyScrolled
                };
                scrollTargets.push(targetData);
            });
            return scrollTargets;
        }

        return {
            restrict: 'A',
            link: function (scope, el) {

                var scrollTargets;
                var scrollableContainer = $(containerSelector);
                var containerOffset = scrollableContainer.offset().top;

                el.on('scroll', function () {
                    scrollTargets = scrollTargets || collectScrollTargets(scrollableContainer);
                    var nearestHidden = findNearestHiddenTarget();
                    sendEventWhenHiddenChanged(nearestHidden);
                });

                scope.$on(events.diffFileSelected, function(event, data) {
                    scrollTargets = scrollTargets || collectScrollTargets(scrollableContainer);
                    var targetOffset = findTargetOffset(data.filename);
                    scrollableContainer.animate({scrollTop: targetOffset - containerOffset});
                });

                scope.$on(events.diffDOMHeightChanged, function() {
                    // invalidate cached offsets, let them to be recalculated when user scrolls or changes file in dropdown
                    scrollTargets = null;
                });

                function findTargetOffset(target) {
                    return scrollTargets.filter(function (t) {
                        return t.filename === target;
                    }).pop().offset;
                }

                function sendEventWhenHiddenChanged(nearestHidden) {
                    if (nearestHidden && sendEventWhenHiddenChanged.currentTarget !== nearestHidden.filename) {
                        sendEventWhenHiddenChanged.currentTarget = nearestHidden.filename;
                        scope.$apply(function () {
                            $rootScope.$broadcast(events.diffScrolledWithFileChange, {filename: nearestHidden.filename});
                        });
                    }
                }

                function findNearestHiddenTarget() {
                    var pixelsAlreadyScrolled = scrollableContainer.scrollTop();
                    return scrollTargets.filter(function (target) {
                        return target.offset - (containerOffset + pixelsAlreadyScrolled) < 1;
                    }).pop();
                }

            }
        }

    });

