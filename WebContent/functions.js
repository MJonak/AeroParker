document.querySelector('#submitButton').disabled = true;

function validateWithinSizeLimit(elem, value, min, max) {
    parent = elem.parentElement;
    if((value < min || value > max)) {
        if(!parent.className.includes("has-error")) {
            parent.className += " has-error";
        }
    } else if (parent.className.includes("has-error")) {
        parent.className.replace("has-error", " ");
    }
}

function validateAllInputsOK(){
    const elements = document.querySelectorAll(".has-error");
    if (elements.length == 0) {
        document.querySelector('#submitButton').disabled = false;
    }
}

