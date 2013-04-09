"use strict";

angular.module("ajaxthrobber", []);

angular.module('codebrag.common.services', []);
angular.module('codebrag.common.filters', []);
angular.module('codebrag.common.directives', ['codebrag.common.services']);

angular.module('codebrag.session', ['ngCookies', 'ui.compat']);

angular.module('codebrag.commits', ['ngResource', 'ui.compat', 'codebrag.commits.comments']);
angular.module('codebrag.commits.comments', []);

angular.module('codebrag', [
    'codebrag.session',
    'codebrag.common.filters',
    'codebrag.common.services',
    'codebrag.common.directives',
    'codebrag.commits',
    'codebrag.followups',
    'ajaxthrobber']);

angular.module('codebrag.session')
    .config(function($stateProvider) {
        $stateProvider
            .state('login', {
                url: '/login',
                templateUrl: 'views/login.html'
            })
            .state('home', {
                url: '/',
                templateUrl: 'views/main.html'
            })
            .state('profile', {
                url: '/profile',
                templateUrl: 'views/secured/profile.html'
            })
    });

angular.module('codebrag.commits')
    .config(function($stateProvider) {
        $stateProvider
            .state('commits', {
                url: '/commits',
                abstract: true,
                templateUrl: 'views/commits.html'
            })
            .state('commits.list', {
                url: '',
                templateUrl: 'views/empty.html'
            })
            .state('commits.details', {
                url: '/{id}',
                templateUrl: 'views/commitDetails.html'
            })
    });

angular.module('codebrag.followups', ['codebrag.commits'])
    .config(function($routeProvider) {
        $routeProvider.
            when("/followups", {controller: 'FollowupsCtrl', templateUrl: "views/secured/followups.html"});
    });

angular.module('codebrag')
    .run(function(authService) {
        authService.requestCurrentUser();
    })

    .config(function($httpProvider) {
        $httpProvider.responseInterceptors.push('httpAuthInterceptor');
        $httpProvider.responseInterceptors.push('httpErrorsInterceptor');
    })

    .run(function ($rootScope, $location, authService, flashService) {
        $rootScope.$on("codebrag:httpAuthError", function(event, data) {
            flashService.set(data.message);
        });
        $rootScope.$on("$routeChangeStart", function (event, next, current) {
            var nextRouteIsSecured = (typeof next.templateUrl !== "undefined") && next.templateUrl.indexOf("/secured/") > -1;
            if (authService.isNotAuthenticated() && nextRouteIsSecured) {
                $location.search("page", $location.url()).path("/login");
            }
        });
        $rootScope.$on("$routeChangeSuccess", function () {
            var message = flashService.get();
            if (angular.isDefined(message)) {
                showInfoMessage(message);
            }
        });
    });