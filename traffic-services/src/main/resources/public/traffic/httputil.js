
function doGet(url, jsonHandler) {
    fetch(url, {
        method: 'GET'
    })
    .then(response => {
        if (!response.ok) {
            var error = new Error('fetch failed:' + response.status);
            error.status = response.status;
            throw error;
        }
        return response.json();
    })
    .then(json => {
        console.log("json=", json);
        jsonHandler(json);
    })
    .catch(error => {
        console.error('There has been a problem with your fetch operation:', error);
   });
}