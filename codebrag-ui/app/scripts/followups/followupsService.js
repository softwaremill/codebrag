angular.module('codebrag.followups')

    .factory('followupsService', function($http, $rootScope, events) {

        var followupsListLocal = new codebrag.followups.LocalFollowupsList();
        var listFetched = false;

        function allFollowups() {
            return _httpRequest('GET').then(function(response) {
                followupsListLocal.addAll(response.data.followupsByCommit);
                listFetched = true;
                return followupsListLocal.collection;
            });
        }

        function removeAndGetNext(followupId, commitId) {
            return _httpRequest('DELETE', followupId, {unique: true, requestId: 'removeFollowup_' + followupId}).then(function() {
                triggerCounterDecrease();
                var nextFollowup = followupsListLocal.removeOneAndGetNext(followupId, commitId);
                return nextFollowup;
            });

        }

        function loadFollowupDetails(followupId) {
            return _httpRequest('GET', followupId).then(function(response) {
                return response.data;
            });
        }

        function hasFollowups() {
            return followupsListLocal.hasFollowups();
        }

        function mightHaveFollowups() {
            return !listFetched || followupsListLocal.hasFollowups()
        }

        function _httpRequest(method, id, config) {
            var followupsUrl = 'rest/followups/' + (id || '');
            var reqConfig = angular.extend(config || {}, {method: method, url: followupsUrl});
            return $http(reqConfig);
        }

        function triggerCounterDecrease() {
            $rootScope.$broadcast(events.followupDone);
        }

        return {
            allFollowups: allFollowups,
            removeAndGetNext: removeAndGetNext,
            loadFollowupDetails: loadFollowupDetails,
            hasFollowups: hasFollowups,
            mightHaveFollowups: mightHaveFollowups
        };

    });

var codebrag = codebrag || {};
codebrag.followups = codebrag.followups || {};

codebrag.followups.LocalFollowupsList = function(collection) {

    var self = this;

    this.collection = collection || [];

    this.addAll = function(newCollection) {
        this.collection.length = 0;
        Array.prototype.push.apply(this.collection, newCollection);
    };

    function nextFollowup(commit, removeAtIndex) {
        var followupToReturn = null;
        var currentCommitIndex = self.collection.indexOf(commit);

        if (commit.followups[removeAtIndex]) {
            followupToReturn = commit.followups[removeAtIndex];
        } else if (self.collection[currentCommitIndex + 1]) {
            followupToReturn = self.collection[currentCommitIndex + 1].followups[0];
        } else if (removeAtIndex > 0) {
            followupToReturn = commit.followups[removeAtIndex - 1];
        } else if (removeAtIndex === 0 && currentCommitIndex > 0) {
            var previousCommitFollowupsLength = self.collection[currentCommitIndex - 1].followups.length;
            followupToReturn = self.collection[currentCommitIndex - 1].followups[previousCommitFollowupsLength - 1];
        }
        if (!commit.followups.length) {
            self.collection.splice(currentCommitIndex, 1);
        }
        return followupToReturn;
    }

    this.removeOneAndGetNext = function(followupId) {
        var currentCommit = _.find(this.collection, function(group) {
            return _.some(group.followups, function(followup) {
                return followup.followupId === followupId;
            });
        });
        var followupToRemove = _.find(currentCommit.followups, function(followup) {
            return followup.followupId === followupId;
        });
        var indexToRemove = currentCommit.followups.indexOf(followupToRemove);
        currentCommit.followups.splice(indexToRemove, 1);
        return nextFollowup(currentCommit, indexToRemove);
    };

    this.hasFollowups = function() {
        return this.collection.length > 0;
    };

    this.followupsCount = function() {
        return _.reduce(this.collection, function(sum, followupsGroup) {
            return sum + followupsGroup.followups.length;
        }, 0);
    };

};

