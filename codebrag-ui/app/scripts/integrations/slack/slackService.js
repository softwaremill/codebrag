(function () {
  'use strict';

  angular.module('codebrag.integrations.slack').factory('slackService', slackService);

  function slackService($q) {

    var slack = {
      userSettings: {}
    };

    var stubData = {
      enabled: true,
      apiToken: 'abc-123',
      notifyOnCommitReviewed: false,
      notifyOnComment: false,
      notifyOnLike: false,
      notifyOnDiscussionReply: false,
      notifyOnNewCommits: false
    };

    slack.load = function () {
      var dfd = $q.defer();
      angular.extend(slack.userSettings, stubData);
      dfd.resolve(slack.userSettings);
      return dfd.promise;
    };

    slack.save = function () {
      var dfd = $q.defer();
      dfd.resolve(slack.userSettings);
      return dfd.promise;
    };

    return slack;

  }

})();

