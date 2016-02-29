angular.module('codebrag.teamMgmt')

    .service('teamMgmtService', function($rootScope, $http, $modal, $q) {

        var teamsApiUrl = 'rest/teams';

        var teams = [];
        teams.pushAll = function(items) {
            Array.prototype.push.apply(this, items);
        };
        
        this.initialize = function() {
            $rootScope.$on('openTeamMgmtPopup', openPopup);
        };

        this.loadTeams = function() {
            return $http.get(teamsApiUrl).then(function(response) {
            	teams.length = 0;
            	teams.pushAll(response.data)
            	return teams;
            });
        };
        
        this.deleteTeam = function(teamData) {
            var deleteTeamUrl = [teamsApiUrl, '/', teamData.teamId].join('');
            return $http.delete(deleteTeamUrl, teamData).then(
	    		function(response) {
	            	teams.splice(findIndexOfTeam(teamData.teamId), 1);
	            }, 
	            function(response) {
	                return $q.reject(response.data || []);
	            }
            );
        };
        
        this.createTeam = function(name) {
            var team = {name: name};

            return $http.post('rest/teams/', team ).then(
                function(response) {
                    teams.push(response.data);
                },
                function(response) {
                    return $q.reject(response.data || []);
                }
            );
        };

        this.loadUsers = function(teamData) {
            var loadUsersUrl = [teamsApiUrl, '/', teamData.id, '/members'].join('');
            return $http.get(loadUsersUrl).then(function(response) {
                return response.data.users;
            });
        };

        this.addTeamMember = function(memberData) {
        	var modifyTeamMemberUrl = [teamsApiUrl, '/', memberData.teamId, '/members'].join('');
        	return $http.post(modifyTeamMemberUrl, memberData).then(
    			function(response) {
    				teams.splice(findIndexOfTeam(memberData.teamId), 1, response.data)
    			},
    			function(response) {
    				return $q.reject(response.data || []);
    			}
			);
        }
        
        this.deleteTeamMember = function(memberData) {
        	var modifyTeamMemberUrl = [teamsApiUrl, '/', memberData.teamId, '/members/', memberData.userId].join('');
        	return $http.delete(modifyTeamMemberUrl).then(
    			function(response) {
    				teams.splice(findIndexOfTeam(memberData.teamId), 1, response.data)
				},
    			function(response) {
    				return $q.reject(response.data || []);
    			}
			);
        }
        
        this.modifyTeamMember = function(memberData) {
        	var modifyTeamMemberUrl = [teamsApiUrl, '/', memberData.teamId, '/members'].join('');
        	return $http.put(modifyTeamMemberUrl, memberData).then(
    			function(response) {
    				teams.splice(findIndexOfTeam(memberData.teamId), 1, response.data)
    			},
    			function(response) {
    				return $q.reject(response.data || []);
    			}
			);
        }
        
        function findIndexOfTeam(teamId) {
        	for (t in teams) {
        		var team = teams[t];
        		if (team.id == teamId) {  
        			return t; 
        		}
        	}
        	return -1;
        }
        
        function openPopup() {
            var config = {
                backdrop: false,
                keyboard: true,
                controller: 'ManageTeamsPopupCtrl',
                templateUrl: 'views/popups/manageTeams.html',
                windowClass: 'manage-teams'
            };
            $modal.open(config)
        }

    });