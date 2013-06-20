$(function() {

// activate comment filed on click
$('.comment-text').on('click', function() {
  $(this).closest('.comment.write').addClass('active');
})

// placement of toggle diff-panel btn
function toggleDiff() {
  if ($('.diff-container').height() <= 600) {
    $('.toggle-diff-container-btn i').css('position', 'relative');
  } else {
    $('.toggle-diff-container-btn i').css('position', 'fixed');
  }

  $('.toggle-diff-container-btn').on('click', function() {
    $('.diff').toggleClass('opened');
    if ($('.diff').hasClass('opened')) {
      $('.commits').animate({opacity: 0}, 500);
      setTimeout(function() {
        $('.toggle-diff-container-btn i').removeClass().addClass('icon-chevron-right')}, 1000);
    } else {
      $('.commits').animate({opacity: 1}, 500);
      $('.toggle-diff-container-btn i').removeClass().addClass('icon-chevron-left');
    }
  })
} 

// opens diff panel fullscreen


function inlineCommentWidthFinder() {
  // make comments in diff table do not overflow to the right if there is a long line of the code in the table
  var i = 0,
      l = $('tbody').length;
      // how many tbodies on the page

  for(; i < l; i++) {

    // check if tbody is inside of the diff-file-container
    if ( $($('tbody')[i]).closest('.diff-file-container')) {

      var tbodyWidth = $($('tbody')[i]).outerWidth(),
          tableWidth = $($('tbody')[i]).closest('.diff-table-wrapper').outerWidth(); 

      // check if tbody is londer than width of the table-container
      if (tbodyWidth > tableWidth) {

        // check if there are any comments in this tbody
        if ( $($('tbody')[i]).find('.comments-container') ) {

          // finding padding
          var commentsContainerPaddingPX = $($('tbody')[i]).find('.comments-container').css('padding-left'),
              commentsContainerPadding = commentsContainerPaddingPX.substring(0, commentsContainerPaddingPX.length - 2),

              commentWidth = tableWidth - 2*commentsContainerPadding;

          // add width to the comment
          $($('tbody')[i]).find('tr.comments-container .comment').css('width', commentWidth);

        }
      }
    }
  }
}

// sticky commits panel
function uiUpdate() {
  // make commits panel sticky and update it's height to make scroll work
  var windowTop = $(window).scrollTop();
  var commitsTop = $('#sticky').offset().top;
  var commitsHeight = $($('.commit-container')[0]).outerHeight()*$('.commit-container').length
  var commitsContainerHeight = $(window).height() - $('.commits-count').outerHeight() - $('.commits-sorting').outerHeight() - $('header').outerHeight();

  // check if there are enough commits to show the scroll
  if (commitsHeight > commitsContainerHeight) {
    $('.commits-container').css('height', commitsContainerHeight).addClass('scroll');
    $('.commits-count').show();
  } else {
    $('.commits-container').removeClass('scroll');
    $('.commits-count').hide();
  }

  if (windowTop > commitsTop) {
    $('.commits').addClass('fixed');

    var commitsContainerHeight = $(window).height() - $('.commits-count').outerHeight() - $('.commits-sorting').outerHeight();

    if (commitsHeight > commitsContainerHeight) {
      $('.commits-container').css('height', commitsContainerHeight).addClass('scroll');
      $('.commits-count').show();
    } else {
      $('.commits-container').removeClass('scroll');
      $('.commits-count').hide();
    }

  } else {
    $('.commits').removeClass('fixed');

    var commitsContainerHeight = $(window).height() - $('.commits-count').outerHeight() - $('.commits-sorting').outerHeight() - ( $('header').outerHeight() - $(window).scrollTop() );
    
    if (commitsHeight > commitsContainerHeight) {
      $('.commits-container').css('height', commitsContainerHeight);
      $('.commits-count').show();
    } else {
      $('.commits-container').removeClass('scroll');
      $('.commits-count').hide();
    }
  }

  inlineCommentWidthFinder();

}

$(window).scroll(uiUpdate);
$(window).resize(uiUpdate);
uiUpdate();

});