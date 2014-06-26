angular.module('codebrag.events', []);

angular.module('codebrag.common.services', ['ui.bootstrap.modal']);
angular.module('codebrag.common.filters', []);
angular.module('codebrag.common.directives', ['codebrag.common.services', 'codebrag.events']);
angular.module('codebrag.common', ['codebrag.common.services', 'codebrag.common.directives', 'codebrag.common.filters']);

angular.module('codebrag.auth', ['codebrag.events']);

angular.module('codebrag.session', ['ui.compat', 'codebrag.auth', 'codebrag.events', 'codebrag.common']);
angular.module('codebrag.notifications', ['codebrag.events', 'codebrag.common', 'codebrag.templates']);
angular.module('codebrag.favicon', ['codebrag.events', 'codebrag.counters']);

angular.module('codebrag.commits.comments', ['ui.compat', 'codebrag.events']);
angular.module('codebrag.commits', [
    'ngResource',
    'codebrag.auth',
    'codebrag.commits.comments',
    'codebrag.events',
    'codebrag.tour',
    'codebrag.branches',
    'codebrag.counters']);

angular.module('codebrag.followups', ['ngResource', 'ui.compat', 'codebrag.auth', 'codebrag.events', 'codebrag.tour']);

angular.module('codebrag.invitations', ['ui.validate', 'ui.keypress']);

angular.module('codebrag.profile', ['codebrag.session']);

angular.module('codebrag.tour', ['codebrag.templates', 'codebrag.auth', 'codebrag.profile', 'codebrag.common']);

angular.module('codebrag.branches', ['codebrag.counters', 'codebrag.events']);

angular.module('codebrag.counters', ['codebrag.branches', 'codebrag.events']);

angular.module('codebrag.licence', ['codebrag.events', 'ui.bootstrap.modal']);

angular.module('codebrag.userMgmt', ['ui.bootstrap.modal']);

angular.module('codebrag', [
    'codebrag.counters',
    'codebrag.templates',
    'codebrag.auth',
    'codebrag.common',
    'codebrag.session',
    'codebrag.commits',
    'codebrag.branches',
    'codebrag.followups',
    'codebrag.notifications',
    'codebrag.favicon',
    'codebrag.tour',
    'codebrag.profile',
    'codebrag.invitations',
    'codebrag.licence',
    'codebrag.userMgmt']);

angular.module('codebrag')
    .config(function ($provide) {
        $provide.decorator('$http', function ($delegate, $q) {
            return codebrag.uniqueRequestsAwareHttpService($delegate, $q);
        });
    })
    .run(function($rootScope, repositoryStatusService, pageTourService, authService, $state, licenceService) {
        repositoryStatusService.checkRepoReady();
        authService.isFirstRegistration().then(openFirstRegistrationIfNeeded);
        pageTourService.initializeTour();
        licenceService.initialize();

        function openFirstRegistrationIfNeeded(firstRegistration) {
            if (firstRegistration) {
                $state.transitionTo('register', {});
            } else {
                authService.requestCurrentUser();
            }
        }
    });

angular.module('codebrag.auth')
    .config(function ($httpProvider) {
        $httpProvider.responseInterceptors.push('httpAuthInterceptor');
        $httpProvider.responseInterceptors.push('httpErrorsInterceptor');
    });

angular.module('codebrag.licence')
    .config(function ($httpProvider) {
        $httpProvider.responseInterceptors.push('httpLicenceExpirationStatusInterceptor');
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
                url: '/{repo}/commits',
                abstract: true,
                templateUrl: 'views/secured/commits/commits.html',
                resolve: authenticatedUser,
                onEnter: function($stateParams, currentRepoContext) {
                    currentRepoContext.ready().then(function() {
                        currentRepoContext.switchRepo($stateParams.repo);
                    })
                }
            })
            .state('commits.list', {
                url: '',
                templateUrl: 'views/secured/commits/emptyCommits.html'
            })
            .state('commits.details', {
                url: '/{sha}',
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


angular.module('codebrag.userMgmt').run(function(userMgmtService) {
    userMgmtService.initialize();
});
