angular.module('codebrag.events', []);

angular.module('codebrag.common.services', ['ui.bootstrap.modal']);
angular.module('codebrag.common.filters', []);
angular.module('codebrag.common.directives', ['codebrag.common.services', 'codebrag.events']);
angular.module('codebrag.common', ['codebrag.common.services', 'codebrag.common.directives', 'codebrag.common.filters']);

angular.module('codebrag.auth', ['codebrag.events']);

angular.module('codebrag.session', ['ui.compat', 'codebrag.auth', 'codebrag.events', 'codebrag.common']);
angular.module('codebrag.notifications', ['codebrag.events', 'codebrag.common', 'codebrag.templates']);
angular.module('codebrag.favicon', ['codebrag.events']);

angular.module('codebrag.commits.comments', ['ui.compat', 'codebrag.events']);
angular.module('codebrag.commits', ['ngResource', 'codebrag.auth', 'codebrag.commits.comments', 'codebrag.events', 'codebrag.tour']);

angular.module('codebrag.followups', ['ngResource', 'ui.compat', 'codebrag.auth', 'codebrag.events', 'codebrag.tour']);

angular.module('codebrag.invitations', ['ui.validate', 'ui.keypress']);

angular.module('codebrag.profile', ['codebrag.session']);

angular.module('codebrag.tour', ['codebrag.templates', 'codebrag.auth', 'codebrag.profile', 'codebrag.common']);


angular.module('codebrag', [
    'codebrag.templates',
    'codebrag.auth',
    'codebrag.common',
    'codebrag.session',
    'codebrag.commits',
    'codebrag.followups',
    'codebrag.notifications',
    'codebrag.favicon',
    'codebrag.tour',
    'codebrag.profile',
    'codebrag.invitations']);

angular.module('codebrag')
    .config(function ($provide) {
        $provide.decorator('$http', function ($delegate, $q) {
            return codebrag.uniqueRequestsAwareHttpService($delegate, $q);
        });
    })
    .run(function($rootScope, repositoryStatusService, pageTourService, authService, $state) {
        repositoryStatusService.checkRepoReady();
        authService.isFirstRegistration().then(openFirstRegistrationIfNeeded);
        pageTourService.initializeTour();

        function openFirstRegistrationIfNeeded(firstRegistration) {
            if (firstRegistration) {
                $state.transitionTo('register', {});
            } else {
                authService.requestCurrentUser();
            }
        }
    });

angular.module('codebrag.favicon')
    .run(function(faviconChangeService) {
        faviconChangeService.setupNotificationWatchers();
    });

angular.module('codebrag.auth')
    .config(function ($httpProvider) {
        $httpProvider.responseInterceptors.push('httpAuthInterceptor');
        $httpProvider.responseInterceptors.push('httpErrorsInterceptor');
    });

angular.module('codebrag.session')
    .config(function ($stateProvider, $urlRouterProvider, authenticatedUser) {
        $urlRouterProvider.when('', '/');
        $stateProvider
            .state('home', {
                url: '/',
                templateUrl: 'views/main.html'
            })
            .state('register', {
                url: '/register/{invitationId}',
                templateUrl: 'views/register.html',
                noLogin: true
            })
            .state('profile', {
                url: '/profile',
                templateUrl: 'views/secured/profile.html',
                resolve: authenticatedUser
            })
            .state('error', {
                url: '/error',
                templateUrl: 'views/errorpages/error500.html',
                noLogin: true
            });
    });

angular.module('codebrag.commits')
    .config(function ($stateProvider, authenticatedUser) {
        $stateProvider
            .state('commits', {
                url: '/commits',
                abstract: true,
                templateUrl: 'views/secured/commits/commits.html',
                resolve: authenticatedUser
            })
            .state('commits.list', {
                url: '',
                templateUrl: 'views/secured/commits/emptyCommits.html'
            })
            .state('commits.details', {
                url: '/{id}',
                templateUrl: 'views/secured/commits/commitDetails.html'
            });
    });

angular.module('codebrag.followups')
    .config(function ($stateProvider, authenticatedUser) {
        $stateProvider
            .state('followups', {
                url: '/followups',
                abstract: true,
                templateUrl: 'views/secured/followups/followups.html',
                resolve: authenticatedUser
            })
            .state('followups.list', {
                url: '',
                templateUrl: 'views/secured/followups/emptyFollowups.html'
            })
            .state('followups.details', {
                url: '/{followupId}/comments/{commentId}',
                templateUrl: 'views/secured/followups/followupDetails.html'
            });
    });

angular.module('codebrag.common')
    .run(function() {
        marked.setOptions({sanitize: true, gfm: true});
    });

