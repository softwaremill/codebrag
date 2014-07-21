angular.module('codebrag.profile')

    .service('emailAliasesService', function($rootScope, $http, $q, authService, events) {

        var aliases = [];
        aliases.pushAll = function(items) {
            Array.prototype.push.apply(this, items);
        };

        function loadAliases() {
            var currentUserId = authService.loggedInUser.id;
            return $http.get('rest/users/' + currentUserId + '/aliases').then(function(response) {
                aliases.length = 0;
                aliases.pushAll(response.data)
            });
        }

        function createAlias(email) {
            var currentUserId = authService.loggedInUser.id;
            var alias = {userId: currentUserId, email: email};
            return $http.post('rest/users/' + currentUserId + '/aliases', alias).then(
                function(response) {
                    aliases.push(response.data);
                    $rootScope.$broadcast(events.profile.emailAliasesChanged)
                },
                function(response) {
                    return $q.reject(response.data || []);
                }
            );
        }

        function deleteAlias(alias) {
            var currentUserId = authService.loggedInUser.id;
            return $http.delete('rest/users/' + currentUserId + '/aliases/' + alias.id).then(
                function() {
                    var index = aliases.indexOf(alias);
                    aliases.splice(index, 1);
                    $rootScope.$broadcast(events.profile.emailAliasesChanged)
                },
                function(response) {
                    return $q.reject(response.data || []);
                }
            );
        }

        return {
            aliases: aliases,
            loadAliases: loadAliases,
            createAlias: createAlias,
            deleteAlias: deleteAlias
        };

    });