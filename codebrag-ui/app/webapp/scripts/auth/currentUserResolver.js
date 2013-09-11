angular.module('codebrag.auth')

    .constant('authenticatedUser', {
        user: function(authService) {
            return authService.requestCurrentUser();
        }
    });
