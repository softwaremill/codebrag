angular.module('codebrag.tour')

    .factory('pageTourService', function($document, $compile, $rootScope, authService, userSettingsService, events) {

        var tourSteps = (function() {

            var steps = {
                commits: { ack: false },
                followups: { ack: false },
                invites: {
                    ack: false,
                    visible: function() {
                        return steps.commits.ack && steps.followups.ack;
                    }
                }
            };

            $rootScope.$on(events.loggedIn, function() {
                Object.getOwnPropertyNames(steps).forEach(function(step) {
                    steps[step].ack = false;
                });
            });

            return steps;

        }());


        var tourDOMAppender = (function() {
            var tourScope, tourDOMEl;
            return {
                remove: function() {
                    if(tourScope && tourDOMEl) {
                        tourDOMEl.remove();
                        tourScope.$destroy();
                    }
                },
                append: function() {
                    var el = angular.element('<page-tour></page-tour>');
                    tourScope = $rootScope.$new();
                    tourDOMEl = $compile(el)(tourScope);
                    var body = $document.find('body').eq(0);
                    body.append(tourDOMEl);
                }
            }
        })();

        function ackStep(stepName) {
            tourSteps[stepName].ack = true;
        }

        function stepActive(stepName) {
            if(tourSteps[stepName].visible) {
                return !tourSteps[stepName].ack && tourSteps[stepName].visible();
            } else {
                return !tourSteps[stepName].ack;
            }
        }

        function initializeTour() {
            $rootScope.$on(events.loggedIn, setupUserTour);

            function setupUserTour() {
                authService.requestCurrentUser().then(function(user) {
                    if(!!!user.settings.welcomeFollowupDone) {
                        tourDOMAppender.append();
                    } else {
                        tourDOMAppender.remove();
                    }
                });
            }
        }

        function finishTour() {
            tourDOMAppender.remove();
            userSettingsService.save({welcomeFollowupDone: true});
        }


        return {
            ackStep: ackStep,
            stepActive: stepActive,
            initializeTour: initializeTour,
            finishTour: finishTour
        }

    });
