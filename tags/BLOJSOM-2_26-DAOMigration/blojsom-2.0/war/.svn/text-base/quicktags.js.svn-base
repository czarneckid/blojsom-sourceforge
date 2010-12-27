// JS QuickTags version 1.0
//
// Copyright (c) 2002-2004 Alex King
// http://www.alexking.org/
//
// Licensed under the LGPL license
// http://www.gnu.org/copyleft/lesser.html
//
// **********************************************************************
// This program is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
// **********************************************************************
//
// This JavaScript will insert the tags below at the cursor position in IE and 
// Gecko-based browsers (Mozilla, Camino, Firefox, Netscape). For browsers that 
// do not support inserting at the cursor position (Safari, OmniWeb) it appends
// the tags to the end of the content.
//
// The variable 'edCanvas' must be defined as the <textarea> element you want 
// to be editing in. See the accompanying 'index.html' page for an example.

var edButtons = new Array();
var edLinks = new Array();
var edOpenTags = new Array();

function edButton(id, display, tagStart, tagEnd, open) {
	this.id = id;				// used to name the toolbar button
	this.display = display;		// label on button
	this.tagStart = tagStart; 	// open tag
	this.tagEnd = tagEnd;		// close tag
	this.open = open;			// set to -1 if tag does not need to be closed
}

edButtons[edButtons.length] = new edButton('ed_bold'
                                          ,'B'
                                          ,'<strong>'
                                          ,'</strong>'
                                          );

edButtons[edButtons.length] = new edButton('ed_italic'
                                          ,'I'
                                          ,'<em>'
                                          ,'</em>'
                                          );

edButtons[edButtons.length] = new edButton('ed_under'
                                          ,'U'
                                          ,'<u>'
                                          ,'</u>'
                                          );

edButtons[edButtons.length] = new edButton('ed_strike'
                                          ,'S'
                                          ,'<s>'
                                          ,'</s>'
                                          );

edButtons[edButtons.length] = new edButton('ed_lt'
                                          ,'&lt;'
                                          ,'&lt;'
                                          ,''
                                          ,-1
                                          );

edButtons[edButtons.length] = new edButton('ed_gt'
                                          ,'&gt;'
                                          ,'&gt;'
                                          ,''
                                          ,-1
                                          );

edButtons[edButtons.length] = new edButton('ed_quot'
                                          ,'&#34;'
                                          ,'&#34;'
                                          ,'&#34;'
                                          ,-1
                                          );

edButtons[edButtons.length] = new edButton('ed_amp'
                                          ,'&#38;'
                                          ,'&#38;'
                                          ,''
                                          ,-1
                                          );

edButtons[edButtons.length] = new edButton('ed_nbsp'
                                          ,'nbsp'
                                          ,'&#160;'
                                          ,''
                                          ,-1
                                          );

edButtons[edButtons.length] = new edButton('ed_nobr'
                                          ,'nobr'
                                          ,'<nobr>'
                                          ,'</nobr>'
                                          );

edButtons[edButtons.length] = new edButton('ed_link'
                                          ,'link'
                                          ,''
                                          ,'</a>'
                                          ); // special case

edButtons[edButtons.length] = new edButton('ed_img'
                                          ,'img'
                                          ,''
                                          ,''
                                          ,-1
                                          ); // special case

edButtons[edButtons.length] = new edButton('ed_ul'
                                          ,'UL'
                                          ,'<ul>\n'
                                          ,'</ul>\n\n'
                                          );

edButtons[edButtons.length] = new edButton('ed_ol'
                                          ,'OL'
                                          ,'<ol>\n'
                                          ,'</ol>\n\n'
                                          );

edButtons[edButtons.length] = new edButton('ed_li'
                                          ,'LI'
                                          ,'\t<li>'
                                          ,'</li>\n'
                                          );

edButtons[edButtons.length] = new edButton('ed_p'
                                          ,'P'
                                          ,'<p>'
                                          ,'</p>\n\n'
                                          );

edButtons[edButtons.length] = new edButton('ed_block'
                                          ,'b-quote'
                                          ,'<blockquote>'
                                          ,'</blockquote>'
                                          );

edButtons[edButtons.length] = new edButton('ed_pre'
                                          ,'pre'
                                          ,'<pre>'
                                          ,'</pre>'
                                          );

function edLink(display, URL, newWin) {
	this.display = display;
	this.URL = URL;
	if (!newWin) {
		newWin = 0;
	}
	this.newWin = newWin;
}


edLinks[edLinks.length] = new edLink('alexking.org'
                                    ,'http://www.alexking.org/'
                                    );

edLinks[edLinks.length] = new edLink('tasks'
                                    ,'http://www.alexking.org/software/tasks/'
                                    );

edLinks[edLinks.length] = new edLink('photos'
                                    ,'http://www.alexking.org/software/photos/'
                                    );

function edShowButton(button, i) {
	if (button.id == 'ed_img') {
		document.write('<input type="button" id="' + button.id + '" class="ed_button" onclick="edInsertImage(edCanvas);" value="' + button.display + '" />');
	}
	else if (button.id == 'ed_link') {
		document.write('<input type="button" id="' + button.id + '" class="ed_button" onclick="edInsertLink(edCanvas, ' + i + ');" value="' + button.display + '" />');
	}
	else {
		document.write('<input type="button" id="' + button.id + '" class="ed_button" onclick="edInsertTag(edCanvas, ' + i + ');" value="' + button.display + '" />');
	}
}

function edShowLinks() {
	var tempStr = '<select onchange="edQuickLink(this.options[this.selectedIndex].value, this);"><option value="-1" selected>(Quick Links)</option>';
	for (i = 0; i < edLinks.length; i++) {
		tempStr += '<option value="' + i + '">' + edLinks[i].display + '</option>';
	}
	tempStr += '</select>';
	document.write(tempStr);
}

function edAddTag(button) {
	if (edButtons[button].tagEnd != '') {
		edOpenTags[edOpenTags.length] = button;
		document.getElementById(edButtons[button].id).value = '/' + document.getElementById(edButtons[button].id).value;
	}
}

function edRemoveTag(button) {
	for (i = 0; i < edOpenTags.length; i++) {
		if (edOpenTags[i] == button) {
			edOpenTags.splice(i, 1);
			document.getElementById(edButtons[button].id).value = 		document.getElementById(edButtons[button].id).value.replace('/', '');
		}
	}
}

function edCheckOpenTags(button) {
	var tag = 0;
	for (i = 0; i < edOpenTags.length; i++) {
		if (edOpenTags[i] == button) {
			tag++;
		}
	}
	if (tag > 0) {
		return true; // tag found
	}
	else {
		return false; // tag not found
	}
}	

function edCloseAllTags() {
	var count = edOpenTags.length;
	for (o = 0; o < count; o++) {
		edInsertTag(edCanvas, edOpenTags[edOpenTags.length - 1]);
	}
}

function edQuickLink(i, thisSelect) {
	if (i > -1) {
		var newWin = '';
		if (edLinks[i].newWin == 1) {
			newWin = ' target="_blank"';
		}
		var tempStr = '<a href="' + edLinks[i].URL + '"' + newWin + '>' 
		            + edLinks[i].display
		            + '</a>';
		thisSelect.selectedIndex = 0;
		edInsertContent(edCanvas, tempStr);
	}
	else {
		thisSelect.selectedIndex = 0;
	}
}

function edSpell(myField) {
	var word = '';
	if (document.selection) {
		myField.focus();
	    var sel = document.selection.createRange();
		if (sel.text.length > 0) {
			word = sel.text;
		}
	}
	else if (myField.selectionStart || myField.selectionStart == '0') {
		var startPos = myField.selectionStart;
		var endPos = myField.selectionEnd;
		if (startPos != endPos) {
			word = myField.value.substring(startPos, endPos);
		}
	}
	if (word == '') {
		word = prompt('Enter a word to look up:', '');
	}
	if (word != '') {
		window.open('http://dictionary.reference.com/search?q=' + escape(word));
	}
}

function edToolbar() {
	document.write('<div id="ed_toolbar">');
	for (i = 0; i < edButtons.length; i++) {
		edShowButton(edButtons[i], i);
	}
	document.write('<input type="button" id="ed_close" class="ed_button" onclick="edCloseAllTags();" value="Close Tags" />');
	document.write('<input type="button" id="ed_spell" class="ed_button" onclick="edSpell(edCanvas);" value="Dict" />');
//	edShowLinks();
	document.write('</div>');
}

// insertion code

function edInsertTag(myField, i) {
	//IE support
	if (document.selection) {
		myField.focus();
	    sel = document.selection.createRange();
		if (sel.text.length > 0) {
			sel.text = edButtons[i].tagStart + sel.text + edButtons[i].tagEnd;
		}
		else {
			if (!edCheckOpenTags(i) || edButtons[i].tagEnd == '') {
				sel.text = edButtons[i].tagStart;
				edAddTag(i);
			}
			else {
				sel.text = edButtons[i].tagEnd;
				edRemoveTag(i);
			}
		}
		myField.focus();
	}
	//MOZILLA/NETSCAPE support
	else if (myField.selectionStart || myField.selectionStart == '0') {
		var startPos = myField.selectionStart;
		var endPos = myField.selectionEnd;
		var cursorPos = endPos;
		if (startPos != endPos) {
			myField.value = myField.value.substring(0, startPos)
			              + edButtons[i].tagStart
			              + myField.value.substring(startPos, endPos) 
			              + edButtons[i].tagEnd
			              + myField.value.substring(endPos, myField.value.length);
			cursorPos += edButtons[i].tagStart.length + edButtons[i].tagEnd.length;
		}
		else {
			if (!edCheckOpenTags(i) || edButtons[i].tagEnd == '') {
				myField.value = myField.value.substring(0, startPos) 
				              + edButtons[i].tagStart
				              + myField.value.substring(endPos, myField.value.length);
				edAddTag(i);
				cursorPos = startPos + edButtons[i].tagStart.length;
			}
			else {
				myField.value = myField.value.substring(0, startPos) 
				              + edButtons[i].tagEnd
				              + myField.value.substring(endPos, myField.value.length);
				edRemoveTag(i);
				cursorPos = startPos + edButtons[i].tagEnd.length;
			}
		}
		myField.focus();
		myField.selectionStart = cursorPos;
		myField.selectionEnd = cursorPos;
	}
	else {
		if (!edCheckOpenTags(i) || edButtons[i].tagEnd == '') {
			myField.value += edButtons[i].tagStart;
			edAddTag(i);
		}
		else {
			myField.value += edButtons[i].tagEnd;
			edRemoveTag(i);
		}
		myField.focus();
	}
}

function edInsertContent(myField, myValue) {
	//IE support
	if (document.selection) {
		myField.focus();
		sel = document.selection.createRange();
		sel.text = myValue;
		myField.focus();
	}
	//MOZILLA/NETSCAPE support
	else if (myField.selectionStart || myField.selectionStart == '0') {
		var startPos = myField.selectionStart;
		var endPos = myField.selectionEnd;
		myField.value = myField.value.substring(0, startPos)
		              + myValue 
                      + myField.value.substring(endPos, myField.value.length);
		myField.focus();
		myField.selectionStart = startPos + myValue.length;
		myField.selectionEnd = startPos + myValue.length;
	} else {
		myField.value += myValue;
		myField.focus();
	}
}

function edInsertLink(myField, i, defaultValue) {
	if (!defaultValue) {
		defaultValue = 'http://';
	}
	if (!edCheckOpenTags(i)) {
		var URL = prompt('Enter the URL' ,defaultValue);
		if (URL) {
			edButtons[i].tagStart = '<a href="' + URL + '">';
			edInsertTag(myField, i);
		}
	}
	else {
		edInsertTag(myField, i);
	}
}

function edInsertImage(myField) {
	var myValue = prompt('Enter the URL of the image', 'http://');
	if (myValue) {
		myValue = '<img src="' 
				+ myValue 
				+ '" alt="' + prompt('Enter a description of the image', '') 
				+ '" />';
		edInsertContent(myField, myValue);
	}
}
