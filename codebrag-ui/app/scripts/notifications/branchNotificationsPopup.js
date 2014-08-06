angular.module('codebrag.notifications')

/*
Directive displaying popup with notifications list (for each watched repo/branch that has commits to review)
Takes collection of "BranchNotification" objects and action to call when given row is clicked (passing BranchNotification)
Displayed on event and hidden when clicked outside.
*/

    .directive('branchNotificationsPopup', function(BranchNotification, FollowupsNotification) {

        var OPEN_POPUP_EVENT = 'openNotificationsPopup';

        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'views/notifications/branchNotificationsPopup.html',
            scope: {
                notifications: '=src',
                onCommitsClick: '&',
                onFollowupsClick: '&'
            },
            link: function(scope, el) {
                el.on('blur', function() {
                    el.hide();
                });

                scope.$on(OPEN_POPUP_EVENT, function() {
                    el.show();
                    el.focus();
                });

                scope.hideAndProceedToRepo = function(notif) {
                    scope.onCommitsClick({notif: notif});
                    el.hide();
                };

                scope.hideAndProceedToFollowups = function() {
                    scope.onFollowupsClick();
                    el.hide();
                }
            },
            controller: function($scope) {

                $scope.displayFollowupsNotifs = function(notif) {
                    return notif instanceof FollowupsNotification;
                };

                $scope.displayBranchNotifs = function(notif) {
                    return !(notif instanceof FollowupsNotification);
                };

                $scope.displayActiveOnly = function(notif) {
                    return notif.active();
                };

                $scope.notificationsAvailable = function() {
                    var active = $scope.notifications.filter(function(n) {
                        return n.active();
                    });
                    return active.length > 0;
                }
            }
        }

    });