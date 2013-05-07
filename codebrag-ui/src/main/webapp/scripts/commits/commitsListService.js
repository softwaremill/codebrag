angular.module('codebrag.commits')

    .factory('commitsListService', function($resource, $q, $http, Commits) {

        var commits = new codebrag.AsyncCollection();


    	function loadCommitsFromServer(filter) {
            var responsePromise = Commits.get({filter: filter}).$then(function(response) {
                return response.data.commits;
            });
            return commits.loadElements(responsePromise);
    	}

        function syncCommits() {
            $http({method: 'POST', url: 'rest/commits/sync'}).success(function(response) {
                commits.elements.length = 0;
                _.forEach(response.commits, function(commit) {
                    commits.elements.push(commit);
                });
            });
        }

        function allCommits() {
            return commits.elements;
        }

        function removeCommit(commitId) {
            var responsePromise = Commits.remove({id: commitId}).$then();
            return commits.removeElement(function(el) {
                return el.id === commitId;
            }, responsePromise);
        }

        function removeCommitAndGetNext(commitId) {
            var responsePromise = Commits.remove({id: commitId}).$then();
            return commits.removeElementAndGetNext(function(el) {
                return el.id === commitId;
            }, responsePromise);
        }

        function loadCommitById(commitId) {
            return Commits.get({id: commitId}).$then(function(response) {
                return response.data;
            });
        }

        return {
            loadCommitsFromServer: loadCommitsFromServer,
            allCommits: allCommits,
            removeCommitAndGetNext: removeCommitAndGetNext,
            removeCommit: removeCommit,
            loadCommitById: loadCommitById,
            syncCommits: syncCommits
		};

    });

