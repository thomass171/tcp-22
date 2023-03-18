
/**
 * errorHandler is called on network error or HTTP code != 2xx. Otherwise successHandler
 * is called.
 */
function getForJson(uri, params, successHandler, errorHandler) {
    fetch(uri)
       .then(response => {
        if (!response.ok) {
            throw new Error("Network response was not OK");
        }
        const contentType = response.headers.get("content-type");
        if (contentType && contentType.indexOf("application/json") !== -1) {
            return response.json().then(data => { successHandler(true, data); });
        } else {
            return response.text().then(text => { successHandler(false, text); });
        }
        })
        .catch((error) => {
            console.log("fetch returned error ", error);
            errorHandler();
        });
}