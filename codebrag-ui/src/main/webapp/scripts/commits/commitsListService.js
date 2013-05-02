angular.module('codebrag.commits')

    .factory('commitsListService', function($resource, $q, $http, Commits) {

        var commits = new codebrag.AsyncCollection();


    	function loadCommitsFromServer(reviewed) {
            var responsePromise = Commits.get({reviewed: reviewed}).$then(function(response) {
                return response.data.commits;
            });
            return commits.loadElements(responsePromise);
    	}

        function syncCommits() {
            Commits.save({id: 'sync'},{})
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

