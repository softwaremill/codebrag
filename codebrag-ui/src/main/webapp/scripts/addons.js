var codebrag = codebrag || {};

/**
 * Module for adding scripts not related with app features and Angular
 */
codebrag.addons = {};

codebrag.addons.betaRibbon = (function() {

    var ribbon = $('.ribbon');
    var infoPopup = $('.popup-page');
    var popupCloseBtn = infoPopup.find('.close-btn');
    var link = infoPopup.find('a[data-uv-lightbox="classic_widget"]');

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