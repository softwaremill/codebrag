"use strict";

angular.module("ajaxthrobber", []);

angular.module('codebrag.common.services', []);
angular.module('codebrag.common.filters', []);
angular.module('codebrag.common.directives', ['codebrag.common.services']);

angular.module('codebrag.session', ['ngCookies'])
    .config(function ($routeProvider) {
        $routeProvider.
            when("/", {controller: 'SessionCtrl', templateUrl: "views/main.html"}).
            when("/login", {controller: 'SessionCtrl', templateUrl: "views/login.html"}).
            when("/profile", {controller: "ProfileCtrl", templateUrl: "views/secured/profile.html"});
    });

angular.module('codebrag.commits.comments', []);
angular.module('codebrag.followups', ['codebrag.commits'])
    .config(function($routeProvider) {
        $routeProvider.
            when("/followups", {controller: 'FollowupsCtrl', templateUrl: "views/secured/followups.html"});
    });

angular.module('codebrag.commits', ['ngResource', 'codebrag.commits.comments'])
    .config(function($routeProvider) {
        $routeProvider.
            when("/commits", {controller: 'CommitsCtrl', templateUrl: "views/commits.html"});
    });

angular.module('codebrag', [
            'codebrag.session',
            'codebrag.common.filters',
            'codebrag.common.services',
            'codebrag.common.directives',
            'codebrag.commits',
            'codebrag.followups',
            'ajaxthrobber'])

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