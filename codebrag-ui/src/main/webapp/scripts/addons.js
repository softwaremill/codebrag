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

    function bindClicks(feedbackForm) {
        [ribbon, popupCloseBtn, link].forEach(function(el) {
            el.on('click', function() {
                infoPopup.toggleClass(openedPopupClass);
            });
        });
        link.on('click', function() {
            feedbackForm.togglePopup();
        });
    }

    return {
        init: bindClicks
    }

}());

codebrag.addons.feedbackForm = (function() {

    var feedbackBtn = $('#uservoice-feedback-btn');
    var formPopup = $('#uservoice-form-popup');
    var closeFormBtn = formPopup.find('.close-btn');

    function openFormPopup() {
        formPopup.toggleClass('opened');
    }

    function togglePopupDisplay() {
        [feedbackBtn, closeFormBtn].forEach(function(e) {
            e.on('click', function() {
                openFormPopup();
            });
        });
    }

    return {
        init: togglePopupDisplay,
        togglePopup: openFormPopup
    }
}());

$(document).ready(function() {
    codebrag.addons.feedbackForm.init();
    codebrag.addons.betaRibbon.init(codebrag.addons.feedbackForm);

});