angular.module('codebrag.profile')

    .controller('EmailAliasesCtrl', function($scope, popupsService, emailAliasesService, Flash) {

        $scope.flash = new Flash();
        $scope.aliases = emailAliasesService.aliases;

        emailAliasesService.loadAliases();

        $scope.openAddAliasPopup = function() {
            $scope.flash.clear();
            popupsService.openAddAliasPopup().result.then(
                function() {
                    $scope.flash.add('info', 'Email alias created');
                },
                function(errors) {
                    $scope.flash.addAll('error', errors);
                }
            );
        };

        $scope.deleteAlias = function(alias) {
            $scope.flash.clear();
            emailAliasesService.deleteAlias(alias).then(
                function() {
                    $scope.flash.add('info', 'Email alias deleted');
                },
                function() {
                    $scope.flash.add('error', 'Unable to delete email alias');
                }
            );
        };

    });