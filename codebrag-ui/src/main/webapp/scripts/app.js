"use strict";
angular.module('codebrag.events', []);

angular.module('codebrag.common.services', []);
angular.module('codebrag.common.filters', []);
angular.module('codebrag.common.directives', ['codebrag.common.services', 'codebrag.events']);
angular.module('codebrag.common', ['codebrag.common.services', 'codebrag.common.directives', 'codebrag.common.filters']);

angular.module('codebrag.auth', ['codebrag.events']);

angular.module('codebrag.session', ['ui.compat', 'codebrag.auth', 'codebrag.events']);
angular.module('codebrag.notifications', ['codebrag.events']);

angular.module('codebrag.commits.comments', ['ui.compat', 'codebrag.events']);
angular.module('codebrag.commits', ['ngResource', 'codebrag.auth', 'codebrag.commits.comments', 'codebrag.events']);

angular.module('codebrag.followups', ['ngResource', 'ui.compat', 'codebrag.auth', 'codebrag.events']);

angular.module('codebrag', [
    'codebrag.auth',
    'codebrag.common',
    'codebrag.session',
    'codebrag.commits',
    'codebrag.followups',
    'codebrag.notifications']);

angular.module('codebrag')
    .run(function(authService) {
        authService.requestCurrentUser();
    });

angular.module('codebrag.auth')
    .config(function($httpProvider) {
        $httpProvider.responseInterceptors.push('httpAuthInterceptor');
        $httpProvider.responseInterceptors.push('httpErrorsInterceptor');
    });

angular.module('codebrag.session')
    .config(function($stateProvider, $urlRouterProvider, authenticatedUser) {
        $urlRouterProvider.when('', '/');
        $stateProvider
            .state('home', {
                url: '/',
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
                templateUrl: 'views/secured/commits/commitDetailsOpt.html'
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
                url: '/{followupId}/comments/{commentId}',
                templateUrl: 'views/secured/followups/followupDetails.html'
            })
    });


