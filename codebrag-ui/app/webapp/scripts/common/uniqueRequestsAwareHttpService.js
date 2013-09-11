var codebrag = codebrag || {};

/**
 * Implementation of angularjs decorator around $http service.
 * This decorator drops request if the same request is already in progress. By default all requests are passed through.
 * To make use of it set 'unique' option on $http request to 'true' and provide 'requestId'
 * Requests with duplicated 'requestId' are dropped and rejected with promise containing 499 HTTP status code
 * @param $http - original angular http service
 * @param $q - promises service to build and reject promise when request is dropped
 * @returns {Function} modified http service
 */
codebrag.uniqueRequestsAwareHttpService = function($http, $q) {

    var DUPLICATED_REQUEST_STATUS_CODE = 499;
    var EMPTY_BODY = '';
    var EMPTY_HEADERS = {};

    var uniqueRequestOptionName = "unique";
    var requestIdOptionName = 'requestId';

    function checkForDuplicates(requestConfig) {
        return !!requestConfig[uniqueRequestOptionName];
    }

    function checkIfDuplicated(requestConfig) {
        var duplicated = $http.pendingRequests.filter(function(pendingReqConfig) {
            return pendingReqConfig[requestIdOptionName] && pendingReqConfig[requestIdOptionName] === requestConfig[requestIdOptionName];
        });
        return duplicated.length > 0;
    }

    function buildRejectedRequestPromise(requestConfig) {
        var dfd = $q.defer();
        var response = {data: EMPTY_BODY, headers: EMPTY_HEADERS, status: DUPLICATED_REQUEST_STATUS_CODE, config: requestConfig};
        console.info('Such request is already in progres, rejecting this one with', response);
        dfd.reject(response);
        return dfd.promise;
    }


    function registerShortcutMethods(modifiedService) {
        ['get', 'delete', 'head', 'jsonp'].forEach(function (name) {
            modifiedService[name] = function (url, config) {
                return modifiedService(extend(config || {}, {
                    method: name,
                    url: url
                }));
            };
        });
        ['post', 'put'].forEach(function (name) {
            modifiedService[name] = function (url, data, config) {
                return modifiedService(extend(config || {}, {
                    method: name,
                    url: url,
                    data: data
                }));
            };
        });
    }

    function delegatePendingRequestsCollection(modifiedHttpService) {
        modifiedHttpService.pendingRequests = $http.pendingRequests;
    }

    var modifiedHttpService = function(requestConfig) {
        if(checkForDuplicates(requestConfig) && checkIfDuplicated(requestConfig)) {
            return buildRejectedRequestPromise(requestConfig);
        }
        return $http(requestConfig);
    };
    registerShortcutMethods(modifiedHttpService);
    delegatePendingRequestsCollection(modifiedHttpService);

    return modifiedHttpService;
};