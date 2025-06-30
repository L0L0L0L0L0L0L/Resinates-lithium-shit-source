/**
 * @function AIO Style NPC
 * @npc Kin (9900000)
 * @note sendStyle can only take a 128 length array (signed byte)
 * @author Gijiko
 
 Selections:
 Skin = 0
 Male Hair = 1
 Male Hair (2) = 2
 Male Hair (3) = 3
 Female Hair = 4
 Female Hair (2) = 5
 Female Hair (3) = 6
 Hair Color = 7
 Male Face = 8
 Male Face (2) = 10
 Female Face = 9
 Female Face (2) = 11
 Specials = 12
 Eye Color = 13
 */
importPackage(Packages.client);
importPackage(Packages.tools);
importPackage(Packages.server);

var status = 0;
var selected;
//var skin = [0, 1, 2, 3, 4, 5, 9, 10];
var skin = [0, 1, 2, 3, 4, 5, 9, 10, 11];
//var hairs = new Array(); // These are used so their selection is remembered for use in status 2
var items = new Array();
var hairs = new Array();
var hr = new Array();
var hair;
var haircolor;
var faces = new Array();
var eye;
var eyecolors;
var choice;

function start() {
    cm.sendSimple("Which nx would u like to see?\r\n\#L1#Skin#l\r\n\#L2#Hair Color#l\r\n\#L3#Eye Color#l\r\n\#L4#Random Hair Styles#l\r\n\#L5#Random Face Styles#l");
    //cm.sendOk("Style NPC coming soon.....");
    //            
}

function action(mode, type, selection) {
    status++;
    if (mode != 1) {
                    
        return;
    }
    if (status == 1) {
        selected = selection;
        if (selection == 1) {
            cm.sendStyle("Choose a style!\r\nThere are " + skin.length + " styles to choose from.", skin);
        } else if (selection == 2) {
            var setHairToBlack = setBlack(cm.getPlayer().getHair(), true);
            haircolor = range(setHairToBlack, setHairToBlack + 7, 1);
            cm.sendStyle("Which color?", haircolor);
        } else if (selection == 3) {
            var setEyeToBlack = setBlack(cm.getPlayer().getFace(), false);
            eyecolors = range(setEyeToBlack, setEyeToBlack + 800, 100);
            cm.sendStyle("Which color?", eyecolors);
        } else if (selection == 4) {
			//hairs = cm.getHairs().slice(1400,1410);
            for (var i = 0; i < 100; i++) {
                hairs.push(cm.getRandomHair());
            }
			//cm.sendOk("Thank you come again. " + hairs);
            cm.sendStyle("Choose a style!\r\nThere are " + hairs.length + " random hair styles to choose from.", hairs);
        } else if (selection == 5) {
            for (var i = 0; i < 100; i++) {
                faces.push(cm.getRandomFace());
            }
			
            cm.sendStyle("Choose a style!\r\nThere are " + faces.length + " random face styles to choose from.", faces);
        }
    } else if (status == 2) {
        if (selected == 1) {
            cm.setSkin(skin[selection]);
        } else if (selected == 2) {
            cm.setHair(haircolor[selection]);
        } else if (selected == 3) {
            cm.setFace(eyecolors[selection]);
        } else if (selected == 4) {
            cm.setHair(hairs[selection]);
        } else if (selected == 5) {
            cm.setFace(faces[selection]);
        }
        cm.sendOk("Thank you come again.");
                    
    }
}

function range(start, stop, increment) { // Apparently JavaScript does not come with this
    var arr = new Array();
    for (var i = start; i <= stop; i += increment) {
        arr.push(i);
    }
    return arr;
}

function setBlack(id, hair) {
    if (hair) {
        return id - (id % 10);
    } else { // eye
        return id - (Math.floor((id / 100) % 10) * 100);
    }
}