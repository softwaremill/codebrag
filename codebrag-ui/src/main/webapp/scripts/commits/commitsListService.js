angular.module('codebrag.commits')

    .factory('commitsListService', function($resource, $q) {

    	var commits = [];
		var commitsResource = $resource('rest/commits/:id', {id: "@id"});

    	function loadCommitsFromServer() {
            commitsResource.get(function(response) {
                commits = response.commits;
            });
    	}

        function allCommits() {
            return commits;
        }

        function removeCommit(commitId) {
            return _removeFromServerAndLocally(commitId);
        }

        function removeCommitAndGetNext(commitId) {
            var currentCommitIndex = _findIndexById(commitId);
            return _removeFromServerAndLocally(commitId).then(function() {
                return _locateNextCommit(currentCommitIndex);
            });
        }

        function rere(commitId) {
            commitsResource.remove({id: commitId}, function() {
                commits.splice(currentCommitIndex, 1);
            });
        }

        function loadCommitById(commitId) {
            return commitsResource.get({id: commitId});
        }

        function _removeFromServerAndLocally(commitId) {
            var currentCommitIndex = _findIndexById(commitId);
            var deferred = $q.defer();
            commitsResource.remove({id: commitId}, function() {
                commits.splice(currentCommitIndex, 1);
                deferred.resolve();
            }, function(err){
                deferred.reject(err);
            });
            return deferred.promise;
        }

        function _findIndexById(commitId) {
            var foundCommit = _.find(commits, function(element) {
                return element.id === commitId;
            });
            return commits.indexOf(foundCommit);
        }

        function _locateNextCommit(currentCommitIndex) {
            if(_.isEmpty(commits)) {
                return null;
            }
            if(currentCommitIndex === 0) {
                return commits[0];
            }
            return commits[currentCommitIndex - 1];
        }

        return {
			loadCommitsFromServer: loadCommitsFromServer,
            allCommits: allCommits,
            removeCommitAndGetNext: removeCommitAndGetNext,
            removeCommit: removeCommit,
            loadCommitById: loadCommitById
		};

    });

