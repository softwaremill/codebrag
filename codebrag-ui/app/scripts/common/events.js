angular.module('codebrag.events', []).constant('events', {

    loginRequired: 'codebrag:loginRequired',
    loggedIn: 'codebrag:loggedIn',
    httpError: 'codebrag:httpError',
    authError: 'codebrag:authError',

    commitsTabOpened: 'codebrag:commitsTabOpened',
    followupsTabOpened: 'codebrag:followupsTabOpened',

    commitsListFilterChanged: 'codebrag:commitsListFilterChanged',
    commitReviewed: 'codebrag:commitReviewed',
    followupDone: 'codebrag:followupDone',

    closeForm: 'codebrag:closeForm',
    scrollOnly: 'codebrag:scrollOnly',

    nextCommitsLoaded: 'codebrag:nextCommitsLoaded',
    previousCommitsLoaded: 'codebrag:previousCommitsLoaded',

    diffScrolledWithFileChange: 'codebrag:diffScrolledWithFileChange',
    diffFileSelected: 'codebrag:diffFileSelected',
    diffDOMHeightChanged: 'codebrag:diffDOMHeightChanged',

    branches: {
        branchChanged: 'codebrag:branchChanged'
    },

    licence: {
        openPopup: 'codebrag:openLicencePopup',
        licenceAboutToExpire: 'codebrag:licenceAboutToExpire',
        licenceExpired: 'codebrag:licenceExpied',
        licenceKeyRegistered: 'codebrag:licenceKeyRegistered'
    }
});