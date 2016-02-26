angular.module('codebrag.events', []);

angular.module('codebrag.common.services', ['ui.bootstrap.modal']);
angular.module('codebrag.common.filters', []);
angular.module('codebrag.common.directives', ['codebrag.common.services', 'codebrag.events']);
angular.module('codebrag.common', ['codebrag.common.services', 'codebrag.common.directives', 'codebrag.common.filters']);

angular.module('codebrag.auth', ['codebrag.events']);

angular.module('codebrag.session', ['ui.compat', 'codebrag.auth', 'codebrag.events', 'codebrag.common']);
angular.module('codebrag.repostatus', ['codebrag.events', 'codebrag.common', 'codebrag.templates']);
angular.module('codebrag.favicon', ['codebrag.events', 'codebrag.notifications']);

angular.module('codebrag.commits.comments', ['ui.compat', 'codebrag.events']);
angular.module('codebrag.commits', [
    'ngResource',
    'codebrag.auth',
    'codebrag.commits.comments',
    'codebrag.events',
    'codebrag.tour',
    'codebrag.branches',
    'codebrag.notifications']);

angular.module('codebrag.followups', ['ngResource', 'ui.compat', 'codebrag.auth', 'codebrag.events', 'codebrag.tour']);

angular.module('codebrag.dashboard', ['ngResource', 'ui.compat', 'codebrag.auth', 'codebrag.events', 'codebrag.tour','codebrag.followups']);

angular.module('codebrag.invitations', ['ui.validate', 'ui.keypress']);

angular.module('codebrag.profile', ['codebrag.session']);

angular.module('codebrag.tour', ['codebrag.templates', 'codebrag.auth', 'codebrag.profile', 'codebrag.common']);

angular.module('codebrag.branches', ['codebrag.notifications', 'codebrag.events']);

angular.module('codebrag.notifications', ['codebrag.branches', 'codebrag.events']);

angular.module('codebrag.userMgmt', ['ui.bootstrap.modal']);

angular.module('codebrag.registration', ['codebrag.branches']);

angular.module('codebrag', [
    'codebrag.notifications',
    'codebrag.templates',
    'codebrag.auth',
    'codebrag.common',
    'codebrag.session',
    'codebrag.registration',
    'codebrag.commits',
    'codebrag.branches',
    'codebrag.followups',
    'codebrag.dashboard',
    'codebrag.repostatus',
    'codebrag.favicon',
    'codebrag.tour',
    'codebrag.profile',
    'codebrag.invitations',
    'codebrag.userMgmt']);

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

angular.module('codebrag.auth')
    .config(function ($httpProvider) {
        $httpProvider.responseInterceptors.push('httpAuthInterceptor');
        $httpProvider.responseInterceptors.push('httpErrorsInterceptor');
    });

angular.module('codebrag.session')
    .config(function ($stateProvider, $urlRouterProvider) {
        $urlRouterProvider.when('', '/');
        $stateProvider
            .state('home', {
                url: '/',
                controller: 'HomeCtrl'
            })
            .state('firstReegistration', {
                url: '/register',
                controller: function($state) {
                    $state.transitionTo('register', {});
                }
            })
            .state('register', {
                url: '/register/{invitationId}',
                templateUrl: 'views/register/register.html',
                controller: 'RegistrationWizardCtrl',
                resolve: {
                    invitationId: function($stateParams) {
                        return $stateParams.invitationId;
                    }
                },
                noLogin: true
            })
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

angular.module('codebrag.dashboard')
.config(function ($stateProvider, authenticatedUser) {
    $stateProvider
        .state('dashboard', {
            url: '/dashboard',
            abstract: true,
            templateUrl: 'views/secured/dashboard/dashboard.html',
            resolve: authenticatedUser
        })
        .state('dashboard.list', {
            url: '',
            templateUrl: 'views/secured/followups/emptyFollowups.html'
        })
        .state('dashboard.details', {
            url: '/{followupId}/comments/{commentId}',
            templateUrl: 'views/secured/dashboard/dashboardDetails.html'
        });
});
