/* Author: aaroncsn(MapleSea Like)(Incomplete)
	NPC Name: 		Parwen
	Map(s): 		Hidden Street: Authorized Person Only(261020401)
	Description: 		Unknown
*/

function start() {
    status = -1;
    action(1, 0, 0);
}

function start() {
    function action(mode, type, selection) {
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            cm.sendOk("You're not ready for this yet.");
        }
    }
}