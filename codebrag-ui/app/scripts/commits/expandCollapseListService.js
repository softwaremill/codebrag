angular.module('codebrag.commits')

    .service('expandCollapseListService', function($window) {

        function commitsList() {
            return $('.commits');
        }

        function chevron() {
            return $('div[expand-collapse-list]').find('i');
        }

        function diffArea() {
            return $('.diff');
        }

        this.expand = function() {
            commitsList().animate({opacity: 0}, 500);
            setTimeout(function () {
                triggerWindowResizeToLetCommentsFormAutoFit();
                chevron().removeClass().addClass('icon-chevron-right');
            }, 1000);
        }

        this.collapse = function() {
            commitsList().animate({opacity: 1}, 500);
            chevron().removeClass().addClass('icon-chevron-left');
            triggerWindowResizeToLetCommentsFormAutoFit();
        };

        this.toggle = function() {
            diffArea().toggleClass('opened');
            if (diffArea().hasClass('opened')) {
                this.expand();
            } else {
                this.collapse();
            }
        };

        function triggerWindowResizeToLetCommentsFormAutoFit() {
            $($window).resize();
        }
    });