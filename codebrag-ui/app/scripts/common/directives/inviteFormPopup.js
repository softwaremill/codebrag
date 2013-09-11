angular.module('codebrag.common.directives').directive('inviteFormPopup', function () {

    function InviteFormPopup($scope, $http) {

        $scope.isVisible = false;

        $scope.submit = function () {
            sendInvitation().then(success, failure);
            function success() {
                $scope.invite = false;
                $scope.success = true;
                clearFormFields();
            }

            function failure() {
                $scope.invite = true;
                $scope.invitation.invitationNotSend = true;
            }
        };

        $scope.$on('openInviteFormPopup', function () {
            $scope.isVisible = true;
            $scope.invite = true;
            createInvitation();
        });

        $scope.inviteMore = function () {
            $scope.invite = true;
            $scope.success = false;
            $scope.invitation.email = "";
            createInvitation();
        };

        $scope.close = function () {
            $scope.isVisible = false;
            delete $scope.success;
            delete $scope.invite;
            clearFormFields();
        };

        function createInvitation() {
            return $http.get('rest/invitation/').then(success, failure);

            function success(resp) {
                $scope.invitation = {
                    body: resp.data.invitation
                };
            }

            function failure() {
                $scope.invitation = {
                    invitationNotCreated: true
                };
            }
        }

        function clearFormFields() {
            $scope.invitation = {};
            $scope.inviteForm.$setPristine();
        }

        function sendInvitation() {
            return $http.post('rest/invitation/', {email: $scope.invitation.email, invitation: $scope.invitation.body}, {bypassInterceptors: true});
        }
    }

    return {
        restrict: 'E',
        replace: true,
        scope: {},
        templateUrl: 'views/inviteForm.html',
        controller: InviteFormPopup
    };

});