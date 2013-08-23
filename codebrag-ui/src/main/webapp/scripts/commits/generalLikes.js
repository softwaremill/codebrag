angular.module('codebrag.commits')

    .directive('generalLikes', function(authService, Likes) {

        function userAlreadyLikedThisCommit(commitLikes) {
            var currentUserId = authService.loggedInUser.id;
            return commitLikes.filter(function(like) {
                return like.authorId === currentUserId;
            }).length > 0;
        }

        function userIsCommitAuthor(commit) {
            var currentUserName = authService.loggedInUser.fullName;
            return commit.info.authorName === currentUserName;
        }

        function userNames(commitLikes) {
            return commitLikes.map(function(like) {
                return like.authorName;
            });
        }

        return {
            restrict: 'E',
            replace: true,
            scope: {
                commit: '='
            },
            templateUrl: 'generalLikes',
            controller: function($scope) {

                $scope.userCannotLike = function() {
                    if(_.isUndefined($scope.commit)) {
                        return false;
                    }
                    var likes = $scope.commit.reactions.likes;
                    return userAlreadyLikedThisCommit(likes)|| userIsCommitAuthor($scope.commit);
                };

                $scope.likeCommit = function() {
                    if(userIsCommitAuthor($scope.commit)) {
                        return;
                    }
                    var likes = $scope.commit.reactions.likes;
                    var like;
                    if(userAlreadyLikedThisCommit(likes)) {
                        var userLike = _.find(likes, function(like) {
                            return like.authorId === authService.loggedInUser.id;
                        });
                        like = {commitId: $scope.commit.info.id, likeId: userLike.id};
                        Likes.delete(like).$then(function(resp) {
                            likes.splice(likes.indexOf(userLike), 1);
                        });
                    } else {
                        like = {commitId: $scope.commit.info.id};
                        Likes.save(like).$then(function(resp) {
                            likes.push(resp.data);
                        });
                    }
                };

                $scope.likeText = function() {
                    if(_.isUndefined($scope.commit)) {
                        return '';
                    }
                    var likes = $scope.commit.reactions.likes;

                    function usersWhoLikeText() {
                        var usersWhoLiked = userNames(likes);
                        var joined = usersWhoLiked.join(', ');
                        return usersWhoLiked.length == 1 ? (joined + ' likes this commit') : (joined + ' like this commit');
                    }

                    function noLikesYetText() {
                        return userIsCommitAuthor($scope.commit) ? "No likes for this commit yet" : "Be the first to like this commit";
                    }

                    return likes.length ? usersWhoLikeText() : noLikesYetText();

                }
            }

        }
    });
