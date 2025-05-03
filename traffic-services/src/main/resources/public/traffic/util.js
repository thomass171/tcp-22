
var uniqueid = 1;

function getUniqueId() {
    return uniqueid++;
}

var StringUtils = {
    substringAfterLast : function(str, sub) {
        var n = str.lastIndexOf(sub);
        if (n == -1) {
            return "";
        }
        str = str.substring(n + sub.length);
        return str;
    },
    substringBeforeLast : function(str, sub) {
        var n = str.lastIndexOf(sub);
        if (n == -1) {
            return "";
        }
        str = str.substring(0, n);
        return str;
    },
    contains : function(str, sub) {
        return str.indexOf( sub ) !== -1;
    }
}

class LocalDateTime {
    constructor(d) {
        if (d == null) {
            d = new Date();
            // date uses UTC
            //d = new Date(d.getTime() - d.getTimezoneOffset() * 60000);
        }

        this.date = d;
    }

    plusMinutes(minutes) {
        return new LocalDateTime(new Date(this.date.getTime() + minutes * 60000));
    }

    plusHours(hours) {
        return new LocalDateTime(new Date(this.date.getTime() + hours * 60 * 60000));
    }

    plusDays(days) {
        return new LocalDateTime(new Date(this.date.getTime() + days * 24 * 60 * 60000));
    }

    //with or without 'T'? currently with. toISOString() always returns UTC.
    toString() {
        return this.toISOLocal();
        var s = this.date.toISOString();
        //console.log("toString:", s);
        s = StringUtils.substringBeforeLast(s, ".");
        //console.log("toString:", s);
        return s;
    }

    toISOLocal() {
        var d = this.date;
        var z = n => ('0' + n).slice(-2);

        return d.getFullYear() + '-'
            + z(d.getMonth() + 1) + '-' +
            z(d.getDate()) + 'T' +
            z(d.getHours()) + ':' +
            z(d.getMinutes()) + ':' +
            z(d.getSeconds());
    }

    //eg. "15.08.2020"
    getUserDate() {
        var d = this.date;
        var z = n => ('0' + n).slice(-2);

        return z(d.getDate()) + '.' +
            z(d.getMonth() + 1) + '.' +
            d.getFullYear();
    }

    // return format YYYY-MM-DD
    getDateString() {
        return this.toString().substring(0, 10);
    }

    getTimeString() {
        return this.toString().substring(11);
    }

    isBefore(otherLocalDateTime) {
        return this.date.getTime() < otherLocalDateTime.date.getTime();
    }

    isAfter(otherLocalDateTime) {
        return this.date.getTime() > otherLocalDateTime.date.getTime();
    }

    getMinute() {
        return this.date.getMinutes();
    }

    getHour() {
        return this.date.getHours();
    }

    //time is HH:MM, no seconds
    atTime(time) {
        var day = this.toString().substr(0, 10);
        return LocalDateTime.parse(day + "T" + time + ":00");
    }

    /**
     * From https://stackoverflow.com/questions/24998624/day-name-from-date-in-js
     */
    getWeekday() {
        var locale = "de-DE";
        //var baseDate = new Date(Date.UTC(2017, 0, 2)); // just a Monday
        //var weekDays = [];
        //for(i = 0; i < 7; i++)
        //{
            return this.date.toLocaleDateString(locale, { weekday: 'long' });
            //baseDate.setDate(baseDate.getDate() + 1);
        //}
        //return weekDays;
    }

    static now() {
        return new LocalDateTime();
    }

    // parses with or without 'T'?
    // see https://stackoverflow.com/questions/33908299/javascript-parse-a-string-to-date-as-local-time-zone
    static parse(s) {
        //console.log("parsing >" + s + "<");
        var b = s.split(/\D/);
        if (b.length == 3) {
            b[3] = 0;
            b[4] = 0;
            b[5] = 0;
        }
        var d = new Date(b[0], b[1] - 1, b[2], b[3], b[4], b[5]);
        /*var d = new Date(Number(s.substring(0, 4)), Number(s.substring(5, 7))-1,
            Number(s.substring(8, 10)), Number(s.substring(11, 13)),
            Number(s.substring(14, 16)), Number(s.substring(17, 19)));*/
        //var d = new Date(Date.parse(s));
        //d = new Date(d.getTime() - d.getTimezoneOffset() * 60000);
        //console.log("parsed " + s + " to ", d.toISOString());
        return new LocalDateTime(d);
    }

    // time is HH:MM, no seconds
    static todayAt(time) {
        return LocalDateTime.now().atTime(time);
    }

    // time is HH:MM, no seconds
    static tomorrowAt(time) {
        var date = LocalDateTime.now().plusDays(1);
        var day = date.toString().substr(0, 10);
        return LocalDateTime.parse(day + "T" + time + ":00");
    }
}

function isUndefined(o) {
    if (o === undefined) {
        return true;
    }
    if (o === null) {
        return true;
    }
    return false;
}

/**
 * Format is YYYY-MM-DD
 */
function date2JsDate(dateString) {
    var jsDate = new Date(dateString.substring(0,4),dateString.substring(5,7)-1,dateString.substring(8,10));
    console.log("jsDate=", key.substring(8,10));
    return jsDate;
}

function setCss(id, property, value) {
    $("#"+id).css(property, value);
}

function addCommonArgs(args, prefix) {
    args.set("vr-controlpanel-posrot",$("#inp_ctrlPanel").val());
    // there are two different fields for offsetvr
    args.set("offsetVR",$("#inp_" + prefix + "offsetVR").val());
    args.set("devmode",$("#chk_devMode").prop("checked"));
    args.set("teamSize",$("#inp_teamSize").val());
}



function launchScene(scenename,args) {

    const params = new URLSearchParams()
    var url = host + "/webgl.html?scene="+scenename;

    args.forEach(function (value, key) {
        //console.log(`${key}: ${value}`);
        //html += '<option value="' + key + '">' + value + '</option>\n';
        url += "&" + key + "=" + value;
    });

    console.log("Opening url ", url);
    var win = window.open(url, '_blank');
}