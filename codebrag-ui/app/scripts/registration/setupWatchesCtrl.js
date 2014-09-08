angular.module('codebrag.registration')

    /*
    Controller for second step of registraiton process
    Having wizard data like invitationCode and registered userId it provides
    repos and branches selection with ability to watch/unwatch branches
     */
    .controller('SetupWatchesCtrl', function($scope, $window, initialWatchesSetupService, registrationWizardData, Flash) {

        $scope.flash = new Flash();
        $scope.wizard = registrationWizardData;
        $scope.branches = initialWatchesSetupService.branches;
        $scope.repos = initialWatchesSetupService.repos;

        initialWatchesSetupService.loadRepos().then(function() {
            $scope.selectRepo($scope.repos[0]);
        });

        $scope.selectRepo = function(repo) {
            $scope.currentRepo = repo;
            initialWatchesSetupService.loadBranches(repo);
        };

        $scope.isSelected = function(repo) {
            return repo === $scope.currentRepo;
        };

        $scope.toggleWatching = function(branch) {
            initialWatchesSetupService.toggleWatching($scope.currentRepo, branch);
        };

        $scope.finish = function() {
            $window.location = '/?registered';
        };

    });
