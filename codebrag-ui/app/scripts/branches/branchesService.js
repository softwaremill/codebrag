angular.module('codebrag.branches')

    .factory('branchesService', function($http, $q, $rootScope, events) {

        var branchesList = [],
            currentBranch,
            repositoryType,
            dataReady = $q.defer(),
            push = Array.prototype.push;

        function loadAvailableBranches() {
            return $http.get('rest/branches').then(function useBranches(response) {
                repositoryType = response.data.repoType;
                branchesList.length = 0;
                push.apply(branchesList, response.data.branches);
                if(angular.isUndefined(currentBranch)) {
                    var userSelectedBranch = $rootScope.loggedInUser.settings.selectedBranch;
                    selectBranch(userSelectedBranch);
                }
                dataReady.resolve();
                return branchesList;
            });
        }

        function allBranches() {
            if(angular.isUndefined(branchesList)) {
                return loadAvailableBranches();
            } else {
                return $q.when(branchesList);
            }
        }

        function selectBranch(branchName) {
            var found = branchesList.filter(function(branch) {
                return branch === branchName
            });
            if(found.length > 0) {
                currentBranch = found[0];
            } else {
                currentBranch = branchesList[0];
            }
            $rootScope.$broadcast(events.branches.branchChanged, currentBranch);
        }

        function selectedBranch() {
            return currentBranch;
        }

        function repoType() {
            return repositoryType;
        }

        function ready() {
            return dataReady.promise;
        }

        function initialize() {
            $rootScope.$on(events.loggedIn, loadAvailableBranches);
        }

        return {
            ready: ready,
            fetchBranches: loadAvailableBranches,
            allBranches: allBranches,
            selectBranch: selectBranch,
            selectedBranch: selectedBranch,
            repoType: repoType,
            initialize: initialize
        }

    });

