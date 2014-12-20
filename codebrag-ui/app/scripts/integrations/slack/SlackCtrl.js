(function () {
  'use strict';

  angular.module('codebrag.integrations.slack').controller('SlackCtrl', SlackCtrl);

  function SlackCtrl($scope, slackService) {

    $scope.slack = slackService.userSettings;
    $scope.saveSettings = slackService.save;

    slackService.load();

  }

})();

