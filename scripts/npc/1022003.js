/* Mr. Thunder
 Victoria Road: Perion (102000000)
 
 Refining NPC: 
 * Minerals
 * Jewels
 * Shields
 * Helmets
 */

var status = -1;
var selectedType = -1;
var selectedItem = -1;
var item;
var mats;
var matQty;
var cost;
var qty;
var equip;

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {

    }
    if (status == 0 && mode == 1) {
        var selStr = "Hm? Who might you be? Oh, you've heard about my forging skills? In that case, I'd be glad to process some of your ores... for a fee.#b";
        var options = new Array("Refine a mineral ore", "Refine a jewel ore", "Refine a crystal ore", "Refine jewel powder");
        for (var i = 0; i < options.length; i++) {
            selStr += "\r\n#L" + i + "# " + options[i] + "#l";
        }

        cm.sendSimple(selStr);
    } else if (status == 1 && mode == 1) {
        selectedType = selection;
        if (selectedType == 0) { //mineral refine
            var selStr = "So, what kind of mineral ore would you like to refine?#b";
            var minerals = new Array("Bronze", "Steel", "Mithril", "Adamantium", "Silver", "Orihalcon", "Gold", "Lidium");
            for (var i = 0; i < minerals.length; i++) {
                selStr += "\r\n#L" + i + "# " + minerals[i] + "#l";
            }
            cm.sendSimple(selStr);
            equip = false;
        } else if (selectedType == 1) { //jewel refine
            var selStr = "So, what kind of jewel ore would you like to refine?#b";
            var jewels = new Array("Garnet", "Amethyst", "Aquamarine", "Emerald", "Opal", "Sapphire", "Topaz", "Diamond", "Black Crystal");
            for (var i = 0; i < jewels.length; i++) {
                selStr += "\r\n#L" + i + "# " + jewels[i] + "#l";
            }
            cm.sendSimple(selStr);
            equip = false;
        } else if (selectedType == 2) { //jewel refine
            var selStr = "So, what kind of crystal ore would you like to refine?#b";
            var jewels = new Array("Power", "Wisdom", "Dex", "Luk", "Dark");
            for (var i = 0; i < jewels.length; i++) {
                selStr += "\r\n#L" + i + "# " + jewels[i] + "#l";
            }
            cm.sendSimple(selStr);
            equip = false;
        } else if (selectedType == 3) { //jewel refine
            var selStr = "So, what magic powder would you like to convert to jewel powder?#b";
            var jewels = new Array("Mighty(Red)", "Lucky(Green)", "Keen(Yellow)", "Nimble(Blue)");
            for (var i = 0; i < jewels.length; i++) {
                selStr += "\r\n#L" + i + "# " + jewels[i] + "#l";
            }
            cm.sendSimple(selStr);
            equip = false;
        }
        if (equip)
            status++;
    } else if (status == 2 && mode == 1) {
        selectedItem = selection;
        if (selectedType == 0) { //mineral refine
            var itemSet = new Array(4011000, 4011001, 4011002, 4011003, 4011004, 4011005, 4011006, 4011008);
            var matSet = new Array(4010000, 4010001, 4010002, 4010003, 4010004, 4010005, 4010006, 4010007);
            var matQtySet = new Array(10, 10, 10, 10, 10, 10, 10, 10);
            var costSet = new Array(300, 300, 300, 500, 500, 500, 800, 1000);
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        } else if (selectedType == 1) { //jewel refine
            var itemSet = new Array(4021000, 4021001, 4021002, 4021003, 4021004, 4021005, 4021006, 4021007, 4021008);
            var matSet = new Array(4020000, 4020001, 4020002, 4020003, 4020004, 4020005, 4020006, 4020007, 4020008);
            var matQtySet = new Array(10, 10, 10, 10, 10, 10, 10, 10, 10);
            var costSet = new Array(500, 500, 500, 500, 500, 500, 500, 1000, 3000);
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        } else if (selectedType == 2) { //jewel refine
            var itemSet = new Array(4005000, 4005001, 4005002, 4005003, 4005004);
            var matSet = new Array(4004000, 4004001, 4004002, 4004003, 4004004);
            var matQtySet = new Array(10, 10, 10, 10, 10);
            var costSet = new Array(5000, 5000, 5000, 5000, 5000);
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        } else if (selectedType == 3) { //jewel refine
            var itemSet = new Array(4008000, 4008001, 4008002, 4008003);
            var matSet = new Array(4007006, 4007003, 4007004, 4007002);
            var matQtySet = new Array(10, 10, 10, 10);
            var costSet = new Array(25000, 25000, 25000, 25000);
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        }

        var prompt = "So, you want me to make some #t" + item + "#s? In that case, how many do you want me to make?\r\nEach Refine Costs 10 Materials to make 1 Refined Product.\r\nI can refine upto 3000 Items at a time.";

        cm.sendGetNumber(prompt, 1, 1, 30000);
    } else if (status == 3 && mode == 1) {
        if (equip)
        {
            selectedItem = selection;
            qty = 1;
        } else
            qty = selection;

        var prompt = "You want me to make ";
        if (qty == 1)
            prompt += "a #t" + item + "#?";
        else
            prompt += qty + " #t" + item + "#?";

        prompt += " In that case, I'm going to need specific items from you in order to make it. Make sure you have room in your inventory, though!#b";

        if (mats instanceof Array) {
            for (var i = 0; i < mats.length; i++) {
                prompt += "\r\n#i" + mats[i] + "# " + matQty[i] * qty + " #t" + mats[i] + "#";
            }
        } else {
            prompt += "\r\n#i" + mats + "# " + matQty * qty + " #t" + mats + "#";
        }

        if (cost > 0)
            prompt += "\r\n#i4031138# " + cost * qty + " meso";

        cm.sendYesNo(prompt);
    } else if (status == 4 && mode == 1) {
        var complete = false;

        if (cm.getMeso() < cost * qty) {
            cm.sendOk("I'm afraid you cannot afford my services.");
            cm.safeDispose();
        } else {
            if (mats instanceof Array) {
                for (var i = 0; i < mats.length; i++) {
                    if (matQty[i] * qty == 1) {
                        complete = cm.haveItem(mats[i]);
                    } else {
                        complete = cm.haveItem(mats[i], matQty[i] * qty);
                    }
                    if (!complete) {
                        break;
                    }
                }
            } else {
                complete = cm.haveItem(mats, matQty * qty);
            }

            if (!complete)
                cm.sendOk("I'm afraid you're missing something for the item you want. See you another time, yes?");
            else {
                if (mats instanceof Array) {
                    for (var i = 0; i < mats.length; i++) {
                        cm.gainItem(mats[i], -matQty[i] * qty);
                    }
                } else
                    cm.gainItem(mats, -matQty * qty);

                cm.gainMeso(-cost * qty);
                cm.gainItem(item, qty);
                cm.sendOk("There, finished. What do you think, a piece of art, isn't it? Well, if you need anything else, you nkow where to find me.");
            }
            cm.safeDispose();
        }
    }
}