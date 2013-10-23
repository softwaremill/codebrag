angular.module('codebrag.commits')
    /**
     * It's kind of delegate that has two possible implemenations for all and pending commits
     * Depending on what caller needs (with prior calls to setAllMode or setToReviewMode)
     * suitable implementation is called.
     *
     * Most of the code here is to create delegates
     */
    .factory('commitsService', function(allCommitsListService, pendingCommitsListService) {

        var toReviewService = true;

        var proxyService = {};


        proxyService.setToReviewMode = function() {
            toReviewService = true;
        };

        proxyService.setAllMode = function() {
            toReviewService = false;
        };

        setupDelegatesOn(proxyService);

        function setupDelegatesOn(dest) {
            var functionsToDelegate = _.union(findFunctionsToDelegate(pendingCommitsListService), findFunctionsToDelegate(allCommitsListService));
            functionsToDelegate.forEach(function(delegate) {
                dest[delegate] = function() {
                    var target = getCurrentImpl();
                    return target[delegate].apply(target, arguments);
                }
            });
        }

        function findFunctionsToDelegate(srcObj) {
            var functions = [];
            for (var p in srcObj) {
                if (srcObj.hasOwnProperty(p) && _.isFunction(srcObj[p])) {
                    functions.push(p);
                }
            }
            return functions;
        }

        function getCurrentImpl() {
            return toReviewService ? pendingCommitsListService : allCommitsListService
        }

        return proxyService;

    });