angular.module('codebrag.branches')

    .factory('branchesService', function($http, $q) {

        var branchesList;
        var currentBranch;

        function loadAvailableBranches() {
            return $http.get('rest/branches').then(function useBranches(response) {
                branchesList = response.data.branches;
                if(angular.isUndefined(currentBranch)) {
                    currentBranch = response.data.current;
                }
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
            (found.length > 0) && (currentBranch = found[0]);
        }

        function selectedBranch() {
            return currentBranch;
        }

        return {
            fetchBranches: loadAvailableBranches,
            allBranches: allBranches,
            selectBranch: selectBranch,
            selectedBranch: selectedBranch
        }

    });

