angular.module('codebrag.notifications')

    .directive('updateIndicator', function(events) {

        return {
            template: '<span ng-show="updatesAvailable" class="ghost-notification"></span>',
            restrict: 'E',
            replace: true,
            scope: {},
            link: function(scope, el, attrs) {
                var watchType = attrs.watch; // 'commits' or 'followups'

                scope.$on(events.updatesWaiting, function(event, data) {
                    scope.updatesAvailable = data[watchType];
                });

                scope.$on(resetEventFor(watchType), function() {
                    scope.updatesAvailable = false;
                });

                function resetEventFor(watchType) {
                    if (watchType === 'commits') {
                        return events.commitCountChanged;
                    } else if (watchType === 'followups') {
                        return events.followupCountChanged;
                    }
                }

            }
        }

    });