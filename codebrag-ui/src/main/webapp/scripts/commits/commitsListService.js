angular.module('codebrag.commits')

    .factory('commitsListService', function($resource, $q, $http) {

        var service = new codebrag.AsyncCollection();

        function _httpRequest(method, id) {
            var commitsUrl = 'rest/commits/' + (id || '');
            return $http({method: method, url: commitsUrl});
        }

    	function loadCommitsFromServer() {
            var requestPromise = _httpRequest('GET').then(function(response) {
                return response.data.commits;
            });
            service.loadElements(requestPromise);
    	}

        function syncCommits() {
            _httpRequest('POST', 'sync').success(function(response) {
                service.elements.length = 0;
                _.forEach(response.commits, function(commit) {
                    service.elements.push(commit);
                });
            });
        }

        function allCommits() {
            return service.elements;
        }

        function removeCommit(commitId) {
            var requestPromise = _httpRequest('DELETE', commitId);
            return service.removeElement(function(el) {
                return el.id === commitId;
            }, requestPromise);
        }

        function removeCommitAndGetNext(commitId) {
            var requestPromise = _httpRequest('DELETE', commitId);
            return service.removeElementAndGetNext(function(el) {
                return el.id === commitId;
            }, requestPromise);
        }

        function loadCommitById(commitId) {
            return _httpRequest('GET', commitId).then(function(response) {
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

