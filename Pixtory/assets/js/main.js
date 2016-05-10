window.onload = function() {
    centerBannerText();
    centerForm();
    setGallery();
    centerGalleryTitle();
};

window.onresize = function() {
    centerBannerText();
    centerForm();
    centerGalleryTitle();
};

function centerGalleryTitle() {
    if (jQuery('#gallery').actual('width') > 711) {
        var x = jQuery('#gallery').actual('height') - jQuery('#gallery-title').actual('height');
        var paddingTop = parseInt(x / 2);
        jQuery('#gallery-title').css('padding-top', paddingTop + 'px');
        jQuery('#gallery-title').css('padding-bottom', (x - paddingTop) + 'px');
    }
    else {
        jQuery('#gallery-title').css('padding-top', '50px');
        jQuery('#gallery-title').css('padding-bottom', '50px');
    }
}

function centerBannerText() {
    var bannerText = jQuery('#banner');
    var bannerTextTop = (jQuery('#header').actual('height') - jQuery('#header h1').actual('height')) - jQuery(window).scrollTop() - 60;
    bannerText.css('top', bannerTextTop+'px');
    bannerText.show();
}

function centerForm() {
    var inputs = jQuery('#contact-container-left input');
    var headings = jQuery('#contact h5');
    inputs.css('margin-top', (10 + headings[1].offsetHeight - headings[0].offsetHeight) + 'px');
}

function setGallery() {
     $("#owl-demo").owlCarousel({

        //autoPlay: 5000,
        items : 1,
        itemsDesktop : [1199,1],
        itemsDesktopSmall : [979,1],
        navigation: true,
        navigationText: ["<i class='fa fa-angle-left'></i>","<i class='fa fa-angle-right'></i>"]

     });

     $("#slide-testimonial").owlCarousel({
        autoPlay: 5000,
        singleItem: true,
        pagination: true,
     });
}

function openPopup(x) {
    jQuery('#popup' + x).css('visibility', 'visible');
    jQuery('#popup' + x).css('opacity', '1');
    document.body.style.overflow = 'hidden';
}

function closePopup(x) {
    jQuery('#popup' + x).css('opacity', '0');
    jQuery('#popup' + x).css('visibility', 'hidden');
    document.body.style.overflow = 'auto';
}

var windowScroll = 0;
var scrollHeight = jQuery('#logo').actual('height') + 20;
var scroll = false;
var headerHeight = jQuery('#header').actual('height');
function moveToInfo() {
    var x = jQuery(window).scrollTop();
    if (x > windowScroll) {
        if (x > scrollHeight && x < headerHeight && scroll == false) {
            scroll = true;
            jQuery.scrollTo('#info', 1000);
            jQuery('#header h1').css('opacity', '0');
            setTimeout(function () {
                scroll = false;
            }, 1000);
            windowScroll = headerHeight;
        }
        else {
            windowScroll = x;
        }
    }
    else if (x < windowScroll) {
        if (x < headerHeight - scrollHeight && scroll == false) {
            scroll = true;
            jQuery.scrollTo('#header', 1000);
            setTimeout(function() { jQuery('#header h1').css('opacity', '1'); }, 600);
            setTimeout(function () {
                scroll = false;
            }, 1000);
            windowScroll = 0;
        }
        else {
            windowScroll = x;
        }
    }
    else {
        windowScroll = x;
    }
}

var storyImageSrc;
jQuery('.gallery-img img').click(function() {
    storyImageSrc = this.src;
    var src = storyImageSrc.split('-');
    this.style.opacity = '0';
    var y = this;
    setTimeout(function() {
        y.src = src[0] + '-' + src[1] + '.png';
        y.style.opacity = '1';
    }, 300);
}, function() {
    this.style.opacity = '0';
    var y = this;
    setTimeout(function() {
        var src = y.src;
        if( parseInt(obj.target.dataset.front)){
            src =  src.replace('-image',"");
            obj.target.dataset.front = 0
        }else{
            src =  src.replace('.png' ,'-image.png');
            obj.target.dataset.front = 1;
        }
        y.src = src;
        y.style.opacity = '1';
    }, 300);
});

jQuery(window).scroll(function() {
    centerBannerText();
    moveToInfo();
});

jQuery('#scrollToContent').click(function(e){
    e.preventDefault();
    jQuery.scrollTo("#info", 1000, { offset:-(jQuery('#header .top').height()), axis:'y' });
});

function preload(arrayOfImages) {
    $(arrayOfImages).each(function(){
        $('<img/>')[0].src = this;
        // Alternatively you could use:
        // (new Image()).src = this;
    });
}

preload([
    '../img/story-1.png',
    '../img/story-2.png',
    '../img/story-3.png',
    '../img/story-4.png',
    '../img/story-5.png'
]);

$(document).ready(function() {
    $('#main-content').fadeIn(1000);
});