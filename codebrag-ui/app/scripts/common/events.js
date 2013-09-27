angular.module('codebrag.events', []).constant('events', {

    loginRequired: 'codebrag:loginRequired',
    loggedIn: 'codebrag:loggedIn',
    httpError: 'codebrag:httpError',
    authError: 'codebrag:authError',

    reloadCommitsList: 'codebrag:reloadCommitsList',

    commitReviewed: 'codebrag:commitReviewed',
    followupDone: 'codebrag:followupDone',
    refreshCommitsCounter: 'codebrag:refreshCommitsCounter',
    refreshFollowupsCounter: 'codebrag:refreshFollowupsCounter',

    updatesWaiting: 'codebrag:updatesWaiting',

    closeForm: 'codebrag:closeForm',
    scrollOnly: 'codebrag:scrollOnly',

    nextCommitsLoaded: 'codebrag:nextCommitsLoaded',
    previousCommitsLoaded: 'codebrag:previousCommitsLoaded',

    diffScrolledWithFileChange: 'codebrag:diffScrolledWithFileChange',
    diffFileSelected: 'codebrag:diffFileSelected',
    diffDOMHeightChanged: 'codebrag:diffDOMHeightChanged'
});