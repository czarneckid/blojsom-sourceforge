    var isMenuHidden = true;
    var allPhotos = 0;
    var currPhoto = 0;
    
    function nextPhoto() {
        currPhoto++;
        if(currPhoto == gallery.length)  {
            currPhoto = 0;
        } 
        document.getElementById("photo").innerHTML = '<img src="' + gallery[currPhoto].src + '" />';
        setPhotoDimensions(document.getElementById("photo").getElementsByTagName("img")[0]);
    }
    
    function previousPhoto() {
        currPhoto--;
        if(currPhoto == -1) {
            currPhoto = (gallery.length - 1);
        } 
        document.getElementById("photo").innerHTML = '<img src="' + gallery[currPhoto].src + '" />';
        setPhotoDimensions(document.getElementById("photo").getElementsByTagName("img")[0]);
    }
    
    function setInitialPhoto() {
        if( window.gallery != null ) {
            document.getElementById("photo").innerHTML = 
                '<img src="' + gallery[0].src + '" alt="Photo" />';
        }
        setPhotoDimensions(document.getElementById("photo").getElementsByTagName("img")[0]);
    }
    
    function menuSwitch() {
        if( isMenuHidden ) {
            document.getElementById("column").style.display = "block";
            isMenuHidden = false;
            setAlbumDisplay();
        } else {
            document.getElementById("column").style.display = "none";
            isMenuHidden = true;
            setAlbumDisplay();
        }
    }
    
    
    function setAlbumDisplay() {
        var entries = document.getElementById("entries"); 
        var showMenu = document.getElementById("showHideMenu"); 
        var entry = entries.getElementsByTagName("div")[0];
        var tab = document.getElementById("showHideTab");
        var tabImg = tab.getElementsByTagName("img")[0];
    
        if( !isMenuHidden ) {
            if( document.all ) {
                entry.getAttributeNode("class").nodeValue = "entryAlt";
                showMenu.style.right = "18em";
            } else {
                entry.setAttribute("class","entryAlt")
            }
            document.getElementById("photo").style.display = "none";
    
            if(document.getElementById("remote")) {
                document.getElementById("remote").style.display = "none";
            }
    
            tabImg.src = dirname + "hidemenu.gif";
        } else {
            tabImg.src = dirname + "showmenu.gif";
            document.getElementById("photo").style.display = "block";
    
            if(document.getElementById("remote")) {
                document.getElementById("remote").style.display = "block";
            }
    
            if( document.all ) {
                entry.getAttributeNode("class").nodeValue = "entry";
                showMenu.style.right = "0";
            } else {
                entry.setAttribute("class","entry")
            }
        }
    }
    
    function setPhotoDimensions( photo ) {
        if( photo != null && photo.width > photo.height ) {
            photo.setAttribute("class", "l");
        } else {
            photo.setAttribute("class", "p");
        }
    }
    
