angular.module('codebrag.notifications')

/*
Directive displaying popup with notifications list (for each watched repo/branch that has commits to review)
Takes collection of "BranchNotification" objects and action to call when given row is clicked (passing BranchNotification)
Displayed on event and hidden when clicked outside.
*/

    .directive('branchNotificationsPopup', function(BranchNotification, FollowupsNotification) {

        function show(el) {
            return function() {
                return el.show();
            }
        }

        function hide(el) {
            return function() {
                return el.hide();
            }
        }

        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'views/notifications/branchNotificationsPopup.html',
            scope: {
                notifications: '=src',
                onClick: '&'
            },
            link: function(scope, el) {
                el.parent('li').hover(show(el), hide(el));
                el.parent('li').click(hide(el));

                scope.hideAndProceed = function(notif) {
                    scope.onClick({notif: notif});
                    hide(el)();
                };

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