var status = 0;
var groupsize = 0;
var item = 4036088;
var amount = 5;
var ach = 0;
var cost = 0;
var mob = 0;
var tier = 0;
var star = "#fUI/UIWindow2.img/ToolTip/Equip/Star/Star2# ";

function start() {
    var selStr = "#rFull list of player Commands:#k\r\n";
    selStr += "#rGeneral Commands#k:\r\n";
    selStr += star + "#b@info#k - shows account info\r\n";
    selStr += star + "#b@stats#k - shows stat info\r\n";
    selStr += star + "#b@pal#k - shows pals stats applied to your char\r\n";
    selStr += star + "#b@monster#k - shows info about mobs on map\r\n";
    selStr += star + "#b@mobinfo#k - shows drop item of mobs on map\r\n";
    selStr += star + "#b@stuck#k - warps u back to safe spot on current map\r\n";
    selStr += star + "#b@say#k - sends message server wide\r\n";
    selStr += star + "#b@save#k - saves character\r\n";
    selStr += star + "#b@skin#k - opens damage skin menu\r\n";
    selStr += star + "#b@mask#k (number) - applies skin u own over top of current equipped skin (Must own skin)\r\n";
    selStr += star + "#b@effects#k - toggles effects on or off\r\n";
    selStr += star + "#b@fix#k - fixes char bugged states\r\n";
    selStr += star + "#b@battle#k - toggle random pal battles in wild\r\n";
    selStr += star + "#b@link#k - shows stats from link chars\r\n";
    selStr += star + "#b@rates#k - shows your current rates\r\n";
    selStr += star + "#b@boss#k - shows boss stats gained from killing bosses\r\n";
    selStr += star + "#b@mastery#k - shows mastery percent's\r\n";
    selStr += star + "#b@ping#k - shows your lag on server - use twice\r\n";
    selStr += star + "#b@server#k - shows server rates and info\r\n";
    selStr += star + "#b@guild#k - shows info about guild your in\r\n";
    selStr += star + "#b@helper#k - toggles navi on or off\r\n";
    selStr += star + "#b@who#k - shows who is online\r\n";
    selStr += star + "#b@exit#k - quickly exit an instance\r\n";
    selStr += star + "#b@level#k - shows level info\r\n";
    selStr += star + "#b@map#k - shows info about current map (DEBUG)\r\n";
    selStr += star + "#b@pos#k - shows map id and position your at (DEBUG)\r\n";
    selStr += star + "#b@compress#k - compresses all damage lines into 1\r\n";
    selStr += star + "#b@cool#k - makes damage lines look compressed in slide format\r\n";
    selStr += star + "#b@totem#k - despawns your totem\r\n";
    selStr += star + "#b@ap#k - applies AP\r\n";
    selStr += star + "#b@bank#k - opens meso bank\r\n";
    selStr += star + "#b@slow#k - force applies slow effect if u move to fast\r\n";
    selStr += star + "#b@battlemode#k - toggles pal trainer battles on/off\r\n";
    selStr += "\r\n";
    selStr += "#rItem Commands#k:\r\n";
    selStr += star + "#b@blacklist#k - toggles blacklisting of items with etc command\r\n";
    selStr += star + "#b@etc#k - opens overflow menu\r\n";
    selStr += star + "#b@item#k - shows all use-etc item counts\r\n";
    selStr += star + "#b@storeetc#k - stores all etc items - linked with @blacklist\r\n";
    selStr += star + "#b@storeuse#k - stores all use items - linked with @blacklist\r\n";
    selStr += star + "#b@loot#k - toggles equip drops or auto recycle\r\n";
    selStr += star + "#b@recycle#k - recycles x equips\r\n";
    selStr += star + "#b@showdroid#k - shows or hides andriods\r\n";
    selStr += star + "#b@bait#k - selects your fishing bait for fishing\r\n";
    selStr += star + "#b@equipdrops#k - toggles ALL equip drops from all mobs/bosses\r\n";
    selStr += star + "#b@etcloot#k - toggles etc to overflow on kill\r\n";
    cm.sendOkS(selStr, 2);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        if (status == 0) {
            return;
        }
        status--;
    }
    if (status == 1) {

    }
    if (status == 2) {

    }
    if (status == 3) {

    }
}