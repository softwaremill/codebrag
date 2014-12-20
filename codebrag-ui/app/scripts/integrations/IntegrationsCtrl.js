(function () {
  'use strict';

  angular.module('codebrag.integrations').controller('IntegrationsCtrl', IntegrationsCtrl);

  function IntegrationsCtrl($scope, $state) {
    $scope.integrations = [
      {name: 'Slack'}
    ];

    $scope.open = function(integration) {
      $state.transitionTo('integrations.details', {name: integration.name});
    }
  }

})();

