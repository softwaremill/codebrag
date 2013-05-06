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
            return followups.loadElements(requestPromise)
    	}

        function allFollowups() {
            return followups.elements;
        }

        function removeFollowup(followupId) {
            var requestPromise = _httpRequest('DELETE', followupId);
            return followups.removeElement(function(el) {
                return el.followupId === followupId;
            }, requestPromise);
        }

        function removeFollowupAndGetNext(followupId) {
            var requestPromise = _httpRequest('DELETE', followupId);
            return followups.removeElementAndGetNext(function(el) {
                return el.followupId === followupId;
            }, requestPromise);
        }

        function loadFollowupById(followupId) {
            return _httpRequest('GET', followupId).then(function(response) {
                return response.data;
            });
        }

        return {
			loadFollowupsFromServer: loadFollowupsFromServer,
            allFollowups: allFollowups,
            removeFollowupAndGetNext: removeFollowupAndGetNext,
            removeFollowup: removeFollowup,
            loadFollowupById: loadFollowupById
		};

    });

