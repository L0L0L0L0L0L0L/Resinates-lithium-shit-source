var status = -1;

function action(mode, type, selection) {
    var em = cm.getEventManager("Juliet");
    if (em == null) {
	            
	return;
    }
    if (!cm.canHold(4001131,1)) {
	cm.sendOk("I will need 1 ETC space.");
	            
	return;
    }
    if (cm.getPlayer().getMapId() == 926110000) { //just first stage
	if (java.lang.Math.random() < 0.1) {
	    if (em.getProperty("stage1").equals("0")) {
		em.setProperty("stage1", "1");
		cm.getMap().setReactorState();
	    }
	} else if (java.lang.Math.random() < 0.05) {
	    if (em.getProperty("stage").equals("0")) {
		cm.gainItem(4001131,1);
	    }
	}
    }
                
}