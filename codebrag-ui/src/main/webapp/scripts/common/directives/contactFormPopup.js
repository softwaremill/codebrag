angular.module('codebrag.common.directives').directive('contactFormPopup', function() {

    function ContactFormPopup($scope, $http) {

        $scope.isVisible = false;

        $scope.submit = function() {
            sendFeedbackViaUservoice().then(success, failure);
            function success() {
                $scope.success = true;
                clearFormFields();
            }
            function failure() {
                $scope.failure = true;
            }
        };

        $scope.$on('openContactFormPopup', function() {
            $scope.isVisible = true;
        });

        $scope.close = function() {
            $scope.isVisible = false;
            delete $scope.success;
            delete $scope.failure;
            clearFormFields();
        };

        function clearFormFields() {
            $scope.msg = {};
            $scope.contactForm.$setPristine();
        }

        function sendFeedbackViaUservoice() {
            var apiKey = 'vvT4cCa8uOpfhokERahg';
            var data = {
                format: 'json',
                client: apiKey,
                ticket: {
                    message: $scope.msg.body,
                    subject: $scope.msg.subject
                },
                email: $scope.msg.email
            };
            var url = 'https://codebrag.uservoice.com/api/v1/tickets/create_via_jsonp.json?' + $.param(data) + '&callback=JSON_CALLBACK';
            return $http.jsonp(url);
        }

    }

    return {
        restrict: 'E',
        replace: true,
        scope: {},
        templateUrl: 'views/contactForm.html',
        controller: ContactFormPopup
    };

});