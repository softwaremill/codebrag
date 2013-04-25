angular.module('codebrag.followups')

    .factory('followupsListService', function($resource, $q, $http) {

        var followups = new codebrag.AsyncCollection();

        function _httpRequest(method, id) {
            var followupsUrl = 'rest/followups/' + (id || '');
            return $http({method: method, url: followupsUrl});
        }

    	function loadFollowupsFromServer() {
            var requestPromise = _httpRequest('GET').then(function(response) {
                return response.data.followups;
            });
            followups.loadElements(requestPromise);
    	}

        function allFollowups() {
            return followups.elements;
        }

        function removeFollowup(commitId) {
            var requestPromise = _httpRequest('DELETE', commitId);
            return followups.removeElement(function(el) {
                return el.commit.commitId === commitId;
            }, requestPromise);
        }

        function removeFollowupAndGetNext(commitId) {
            var requestPromise = _httpRequest('DELETE', commitId);
            return followups.removeElementAndGetNext(function(el) {
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

