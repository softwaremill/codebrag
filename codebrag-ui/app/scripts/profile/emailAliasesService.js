angular.module('codebrag.profile')

    .service('emailAliasesService', function($http, $q) {

        var aliases = [];
        aliases.pushAll = function(items) {
            Array.prototype.push.apply(this, items);
        };

        function loadAliases() {
            return _load().then(function(response) {
                aliases.length = 0;
                aliases.pushAll(response.data)
            });
        }

        function createAlias(alias) {
            return _create(alias).then(
                function(response) {
                    aliases.push(response.data);
                },
                function(response) {
                    return $q.reject(response.data.errors || []);
                }
            );
        }

        function deleteAlias(alias) {
            return _remove(alias).then(function() {
                var index = aliases.indexOf(alias);
                aliases.splice(index, 1);
            });
        }

        function _load() {
            var aliases = [
                { id: 1, alias: 'mail@codebrag.com'},
                { id: 2, alias: 'michal.ostruszka@codebrag.com'}
            ];
            return $q.when({data: aliases});
        }

        function _create(email) {
            console.log('creating alias', email);
            if(email.indexOf('fail') != -1) {
                var errors = {
                    general: ['This email is already defined', 'Woohoo bad email']
                };
                return $q.reject({data: { errors: errors }});
            }
            return $q.when({data: {id: 999, alias: email, userId: 123}});
        }

        function _remove(alias) {
            console.log('removing alias', alias);
            return $q.when();
        }

        return {
            aliases: aliases,
            loadAliases: loadAliases,
            createAlias: createAlias,
            deleteAlias: deleteAlias
        };

    });