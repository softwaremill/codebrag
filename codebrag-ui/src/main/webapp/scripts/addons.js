var codebrag = codebrag || {};

/**
 * Module for adding scripts not related with app features and Angular
 */
codebrag.addons = {};

codebrag.addons.betaRibbon = (function() {

    var ribbon = $('.ribbon');
    var infoPopup = $('#early-version-popup');
    var popupCloseBtn = infoPopup.find('.close-btn');
    var link = infoPopup.find('a');

    var openedPopupClass = 'opened';

    function togglePopupPage() {
        [ribbon, popupCloseBtn, link].forEach(function(el) {
            el.on('click', function() {
                infoPopup.toggleClass(openedPopupClass);
            });
        });
    }

    return {
        init: togglePopupPage
    }

}());

$(document).ready(function() {

    codebrag.addons.betaRibbon.init();

});