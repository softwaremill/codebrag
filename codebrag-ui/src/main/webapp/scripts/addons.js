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

codebrag.addons.feedbackForm = (function() {

    var feedbackBtn = $('#uservoice-feedback-btn');
    var formPopup = $('#uservoice-form-popup');
    var closeFormBtn = formPopup.find('.close-btn');

    function togglePopupDisplay() {
        [feedbackBtn, closeFormBtn].forEach(function(e) {
            e.on('click', function() {
                console.log('aa');
                formPopup.toggleClass('opened');
            });
        });
    }

    return {
        init: togglePopupDisplay
    }

}());

$(document).ready(function() {

    codebrag.addons.betaRibbon.init();
    codebrag.addons.feedbackForm.init();

});