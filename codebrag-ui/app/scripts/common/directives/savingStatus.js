/**
 * Replaces EL text with correct value and toggle correct css class
 * usage <EL saving-status="status_property"></EL>
 */
angular.module('codebrag.common.directives')

    .directive('savingStatus', function($timeout) {

        var statusToText = {
            "pending": 'Saving...',
            "success": 'Saved',
            "failed": 'Something went wrong, not saved',
            allClasses: function() {
                return Object.keys(this);
            }
        };

        var hideTimerHandler;

        return {
            restrict: 'A',
            link: function(scope, el, attrs) {

                scope.$watch(attrs.savingStatus, function(changedStatus) {
                    if(changedStatus) {
                        showStatusInfo(changedStatus);
                        scheduleStatusInfoFadeOut(changedStatus);
                    }
                });

                function showStatusInfo(changedStatus) {
                    $timeout.cancel(hideTimerHandler);
                    removeStatusClasses();
                    el.addClass(changedStatus);
                    el.text(statusToText[changedStatus]);
                    el.show();
                }

                function scheduleStatusInfoFadeOut(status) {
                    if(status !== 'pending') {
                        hideTimerHandler = $timeout(function() {
                            el.fadeOut();
                        }, 2000);
                    }
                }

                function removeStatusClasses() {
                    statusToText.allClasses().forEach(function(clazz) {
                        el.removeClass(clazz);
                    });
                }
            }
        };

    });
