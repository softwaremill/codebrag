(function () {
  'use strict';

  angular
    .module('codebrag.integrations', [
      'ui.compat',
      'codebrag.auth',
      'codebrag.integrations.slack'
    ])
    .config(config);

  function config($stateProvider, authenticatedUser) {
    $stateProvider
      .state('integrations', {
        url: '/integrations',
        templateUrl: 'scripts/integrations/integrations.html',
        controller: 'IntegrationsCtrl',
        resolve: authenticatedUser,
        abstract: true
      })
      .state('integrations.list', {
        url: '',
        template: ''
      })
      .state('integrations.details', {
        url: '/{name}',
        templateUrl: function($stateParams) {
          return integrationTemplate($stateParams.name);
        }
      })
  }

  function integrationTemplate(integrationName) {
    var templateName = integrationName + '.html';
    var templateDir = integrationName;
    return ['scripts/integrations', templateDir, templateName].join('/');
  }

})();

