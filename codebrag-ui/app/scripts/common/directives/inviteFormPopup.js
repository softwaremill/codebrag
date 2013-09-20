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

            function failure(response) {
                if (!!!response.dropped) {
                    $scope.invite = true;
                    $scope.invitation.invitationNotSend = true;
                }
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
            return _sendHttpRequest({email: $scope.invitation.email, invitation: $scope.invitation.body});
        }

        function _sendHttpRequest(data) {
            var invitationUrl = 'rest/invitation/';
            var reqConfig = {method: 'POST', url: invitationUrl, unique: true, data: data, requestId: 'sendInvitation', bypassInterceptors: true};
            return $http(reqConfig);
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