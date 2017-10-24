var contentElementId = "contentBody";
var tmpSpan;

window.addEventListener('load', function() {
  document.getElementById(contentElementId).addEventListener("keyup", contentBodyOnKeyUpEvent);
})

/**
 * 1. OnKeyUpEvent get the current cursor position & send it to Java.
*/
function contentBodyOnKeyUpEvent(){
  if (window.getSelection) {
        var sel = window.getSelection();
        if (sel.getRangeAt && sel.rangeCount) {
            var range = sel.getRangeAt(0);
            if (tmpSpan == null) {
               tmpSpan = document.createElement('span');
            }
            range.insertNode(tmpSpan);
            var x = tmpSpan.getBoundingClientRect().left + window.scrollX;
            var y = tmpSpan.getBoundingClientRect().top + window.scrollY;
            injectedObject.onContentKeyUpEventPosition(x, y);

            tmpSpan.parentNode.removeChild(tmpSpan);
        }
  }
}

/**
 * Get Content & send it to Java.
*/
function getContent(){
    var content = document.getElementById(contentElementId).innerHTML;
    injectedObject.onContentReceived(content);
}

/**
 *  1. Inserts the given content at the userSelection.
 *  2. Reference - https://stackoverflow.com/questions/6690752/insert-html-at-caret-in-a-contenteditable-div
*/
function insertContentAtUserSelection(content) {
    document.getElementById(contentElementId).focus()

    var sel, range;
    if (window.getSelection) {
        sel = window.getSelection();
        if (sel.getRangeAt && sel.rangeCount) {
            range = sel.getRangeAt(0);
            range.deleteContents();

            var el = document.createElement("div");
            el.innerHTML = content;
            var frag = document.createDocumentFragment(), node, lastNode;
            while ( (node = el.firstChild) ) {
                lastNode = frag.appendChild(node);
            }
            range.insertNode(frag);

            // Preserve the selection
            if (lastNode) {
                range = range.cloneRange();
                range.setStartAfter(lastNode);
                range.collapse(true);
                sel.removeAllRanges();
                sel.addRange(range);
            }
        }
    }
}

/**
 * Sets the cursor position at the given Html Document ElementId.
*/
function setTheCursorPositionAtTheGivenElementId(elementId){
    var cursorNode = document.getElementById(elementId);
    // If we don't have a element with the givenId then select start of "ContentBody"
    if(!cursorNode){
      cursorNode = document.getElementById(contentElementId);
    }
    var range = document.createRange();
    range.setStart(cursorNode, 0);
    range.setEnd(cursorNode, 0);
    var sel = window.getSelection();
    sel.removeAllRanges();
    sel.addRange(range);
}