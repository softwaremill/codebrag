"use strict";

angular.module("ajaxthrobber", []);

angular.module('codebrag.common.services', []);
angular.module('codebrag.common.filters', []);
angular.module('codebrag.common.directives', ['codebrag.common.services']);
angular.module('codebrag.common', ['codebrag.common.services', 'codebrag.common.directives', 'codebrag.common.filters']);

angular.module('codebrag.session', ['ngCookies', 'ui.compat']);

angular.module('codebrag.commits.comments', ['ui.compat']);
angular.module('codebrag.commits', ['ngResource', 'codebrag.session', 'codebrag.commits.comments']);

angular.module('codebrag.followups', ['ngResource', 'ui.compat', 'codebrag.session']);

angular.module('codebrag', [
    'codebrag.common',
    'codebrag.session',
    'codebrag.commits',
    'codebrag.followups',
    'ajaxthrobber']);

angular.module('codebrag.session').constant('authenticatedUser', {
    user: function(authService) {
        return authService.requestCurrentUser();
    }
});

angular.module('codebrag.session')
    .config(function($stateProvider, authenticatedUser) {
        $stateProvider
            .state('login', {
                url: '/login',
                templateUrl: 'views/login.html'
            })
            .state('home', {
                url: '',
                templateUrl: 'views/main.html'
            })
            .state('profile', {
                url: '/profile',
                templateUrl: 'views/secured/profile.html',
                resolve: authenticatedUser
            })
    });

angular.module('codebrag.commits')
    .config(function($stateProvider, authenticatedUser) {
        $stateProvider
            .state('commits', {
                url: '/commits',
                abstract: true,
                templateUrl: 'views/secured/commits/commits.html',
                resolve: authenticatedUser
            })
            .state('commits.list', {
                url: '',
                templateUrl: 'views/secured/empty.html'
            })
            .state('commits.details', {
                url: '/{id}',
                templateUrl: 'views/secured/commits/commitDetails.html'
            })
    });

angular.module('codebrag.followups')
    .config(function($stateProvider, authenticatedUser) {
        $stateProvider
            .state('followups', {
                url: '/followups',
                abstract: true,
                templateUrl: 'views/secured/followups/followups.html',
                resolve: authenticatedUser
            })
            .state('followups.list', {
                url: '',
                templateUrl: 'views/secured/empty.html'
            })
            .state('followups.details', {
                url: '/{id}',
                templateUrl: 'views/secured/commits/commitDetails.html'
            })
    });

angular.module('codebrag')
    .config(function($httpProvider) {
        $httpProvider.responseInterceptors.push('httpAuthInterceptor');
        $httpProvider.responseInterceptors.push('httpErrorsInterceptor');
    })

    .run(function ($rootScope, $state, $stateParams) {
        $rootScope.$on("codebrag:httpAuthError", function(event, data) {
            $state.transitionTo('login');
        });
    });