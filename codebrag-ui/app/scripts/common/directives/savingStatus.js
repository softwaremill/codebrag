/**
 * Replaces EL text with correct value and toggle correct css class
 * usage <EL saving-status="status_property"></EL>
 */
angular.module('codebrag.common.directives')

    .directive('savingStatus', function() {

        var statusToText = {
            "pending": 'Saving...',
            "success": 'Saved',
            "failed": 'Something went wrong, not saved',
            allClasses: function() {
                return Object.keys(this);
            }
        };

        return {
            restrict: 'A',
            link: function(scope, el, attrs) {
                scope.$watch(attrs.savingStatus, function(changedStatus) {
                    if(changedStatus) {
                        removeStatusClasses();
                        el.addClass(changedStatus);
                        el.text(statusToText[changedStatus])
                    }
                });

                function removeStatusClasses() {
                    statusToText.allClasses().forEach(function(clazz) {
                        el.removeClass(clazz);
                    });
                }
            }
        };

    });
