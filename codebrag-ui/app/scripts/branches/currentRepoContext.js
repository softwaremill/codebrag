angular.module('codebrag.branches')

    .factory('currentRepoContext', function($state, $stateParams, authService, $rootScope, $q, events, UserBrowsingContext) {

        var currentContext,
            contextReady = $q.defer(),
            TO_REVIEW = 'pending',
            ALL = 'all';

        currentContext = {
            all: {},
            commitsFilter: TO_REVIEW,
            repo: null,
            branch: null,

            ready: function () {
                return contextReady.promise;
            },

            isToReviewFilterSet: function () {
                return this.commitsFilter === TO_REVIEW;
            },

            switchBranch: function (newBranch) {
                this.branch = newBranch;
                this.all[this.repo] = newBranch;
                $rootScope.$broadcast(events.branches.branchChanged, newBranch);
                saveUserContext(this);
            },

            switchRepo: function (newRepo) {
                if(this.repo === newRepo || angular.isUndefined(this.all[newRepo])) return;
                this.repo = newRepo;
                this.switchBranch(this.all[newRepo]);
            },

            switchCommitsFilter: function (newFilter) {
                this.commitsFilter = (newFilter === TO_REVIEW ? newFilter : ALL);
                $rootScope.$broadcast(events.commitsListFilterChanged, this.commitsFilter);
            }
        };

        function saveUserContext(params) {
            var context = new UserBrowsingContext(params);
            context.$save();
        }

        authService.userAuthenticated.then(function(user) {
            currentContext.branch = user.browsingContext.branchName;
            currentContext.repo  = user.browsingContext.repoName;
            UserBrowsingContext.query().$then(function(response) {
                response.data.forEach(function(context) {
                    currentContext.all[context.repoName] = context.branchName;
                });
                contextReady.resolve();
            })
        });

        $rootScope.currentRepoContext = currentContext;     //FIXME: for now, to get access to switching repo

        return currentContext;

    });