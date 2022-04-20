
var uniqueid = 1;

function getUniqueId() {
    return uniqueid++;
}

function createDiv(content, optclass = "") {
    var id = "div_" + getUniqueId();
    var html = '<div class=" ' + optclass + '" ';
    html += 'id="' + id + '" ';
    html += ">";
    html += content;
    html += '</div>';

    return {html: html, id: id};
}

function createClickableDiv(content, onclickCode, optclass = "") {
    var id = "div_" + getUniqueId();
    var html = '<div class=" ' + optclass + '" ';
    html += 'id="' + id + '" ';
    html += 'onclick="' + onclickCode + '" ';
    html += ">";
    html += content;
    html += '</div>';

    return {html: html, id: id};
}

function createTable(header, optclass = "") {
    var id = "table_" + getUniqueId();
    var bodyid = "body_" + getUniqueId();
    var html = '<table class=" ' + optclass + '" ';
    html += 'id="' + id + '" ';
    html += "><thead>";
    if (header != null) {
    }
    html += "</thead>";
    html += '<tbody id="' + bodyid + '">';
    html += '</tbody></table>';
    html += '</table>';

    return {html: html, id: id, bodyid: bodyid};
}

function addTableRow(bodyid, optclass) {
    //console.log("Adding row to body", bodyid);
    var row_id = "table_row_" + getUniqueId();
    var row = "<tr id='" + row_id + "' class=' " + optclass + " '>"
    row += "</tr>";
    $("#" + bodyid).append(row);
    return row_id;
}

function addTableCol(content,rowid, optclass) {

    var col_id = "table_col_" + getUniqueId();
    var col = "<td id='" + col_id + "' class=' " + optclass + " '>"
    col += content;
    col += "</td>";
    $("#" + rowid).append(col);
    return col_id;
}

function createSelectBoxForMapOrArray(mapOrArray, addEmpty, optclass = "") {
    var id = "sb_" + getUniqueId();
    var html = '<select class="' + optclass + '  "';
    html += 'id="' + id + '">';
    if (addEmpty) {
        html += '<option value=""></option>\n';
    }
    if (Array.isArray(mapOrArray)) {
        mapOrArray.forEach(value => {
            //console.log(`${key}: ${value}`);
            html += '<option value="' + value + '">' + value + '</option>\n';
        });
    } else {
        mapOrArray.forEach(function (value, key) {
            //console.log(`${key}: ${value}`);
            html += '<option value="' + key + '">' + value + '</option>\n';
        });
    }
    html += '</select>';
    return {html: html, id: id};
}

function createInput(optclass = "") {
    var id = "input_" + getUniqueId();
    var html = '<input class=" ' + optclass + '" ';
    html += 'id="' + id + '" ';
    html += ">";
    html += '</input>';

    //console.log(html);
    return {html: html, id: id};
}

function createCheckbox(optclass = "") {
    var id = "checkbox_" + getUniqueId();
    var html = '<input type="checkbox" class=" ' + optclass + '" ';
    html += 'id="' + id + '" ';
    html += ">";
    html += '</input>';
    return {html: html, id: id};
}

function createButton(content, optclass = "") {
    var id = "btn_" + getUniqueId();
    var html = '<button class=" ' + optclass + '" ';

    html += 'id="' + id + '" ';
    html += ">" + content;
    html += '</button>';

    // console.log(html);
    return {html: html, id: id};
}

function addListItem(listid, content, optclass) {
    var item_id = "list_item_" + getUniqueId();
    var item = "<li id='" + item_id + "' class=' " + optclass + " '>"
    item += content;
    item += "</li>";
    $("#" + listid).append(item);
    return item_id;
}
