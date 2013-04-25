angular.module('codebrag.followups')

    .factory('followupsListService', function($resource, $q, $http) {

        var collection = new codebrag.AsyncCollection();

        function _httpRequest(method, id) {
            var followupsUrl = 'rest/followups/' + (id || '');
            return $http({method: method, url: followupsUrl});
        }

    	function loadFollowupsFromServer() {
            var requestPromise = _httpRequest('GET').then(function(response) {
                return response.data.followups;
            });
            collection.loadElements(requestPromise);
    	}

        function allFollowups() {
            return collection.elements;
        }

        function removeFollowup(commitId) {
            var requestPromise = _httpRequest('DELETE', commitId);
            return collection.removeElement(function(el) {
                return el.commit.commitId === commitId;
            }, requestPromise);
        }

        function removeFollowupAndGetNext(commitId) {
            var requestPromise = _httpRequest('DELETE', commitId);
            return collection.removeElementAndGetNext(function(el) {
                return el.commit.commitId === commitId;
            }, requestPromise);
        }

        return {
			loadFollowupsFromServer: loadFollowupsFromServer,
            allFollowups: allFollowups,
            removeFollowupAndGetNext: removeFollowupAndGetNext,
            removeFollowup: removeFollowup
		};

    });

