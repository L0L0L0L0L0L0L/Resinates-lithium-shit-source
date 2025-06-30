/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc> 
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License version 3
 as published by the Free Software Foundation. You may not use, modify
 or distribute this program under any other version of the
 GNU Affero General Public License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package scripting;

import javax.script.Invocable;

import client.MapleClient;
import client.maplepal.MaplePalBattleManager;
import constants.GameConstants;
import java.util.concurrent.locks.Lock;
import javax.script.ScriptException;

public class NPCScriptManager extends AbstractScriptManager {

    // private Map<MapleClient, NPCConversationManager> cms = new
    // ConcurrentHashMap<MapleClient, NPCConversationManager>();
    // private Map<MapleClient, Invocable> scripts = new
    // ConcurrentHashMap<MapleClient, Invocable>();
    private final static NPCScriptManager instance = new NPCScriptManager();
    public boolean npclock = false;

    public static final NPCScriptManager getInstance() {
        return new NPCScriptManager();
    }

    public boolean isNpcScriptAvailable(MapleClient c, String fileName) {
        Invocable iv = null;
        if (fileName != null) {
            iv = getInvocable("npc/" + fileName + ".js", c);
        }

        return iv != null;
    }

    public final void start(final MapleClient c, final int npc, String filename) {
        if (c == null || GameConstants.getLock()) {
            return;
        }
        try {
            c.setChat(true);
            if (c.canClickNPC()) {
                if (!GameConstants.isBlockedNpc(npc) && c.getPlayer().getBattleMode()) {
                    GameConstants.addNpc(c.getPlayer(), npc);
                    filename = "pal_battle";
                }
                Invocable iv = getInvocable("npc/" + filename + ".js", c);

                if (iv == null) {
                    iv = getInvocable("npc/" + npc + ".js", c);
                    if (iv == null) {
                        startNPC(c, npc, "moral_quest");
                        //MaplePalBattleManager.battleNpc(c.getPlayer(), npc);
                        //dispose(c);
                        return;
                    }
                }
                NPCConversationManager cm = new NPCConversationManager(c, npc, -1, (byte) -1, iv, filename);
                c.setCMS(cm);
                c.setNpcScript(iv);
                engine.put("cm", cm);

                c.getPlayer().setConversation(1);
                c.setClickedNPC();
                // System.out.println("NPCID started: " + npc);
                try {

                    iv.invokeFunction("start"); // Temporary until I've removed all of start
                } catch (NoSuchMethodException nsme) {
                    // System.out.println("action start");
                    iv.invokeFunction("action", (byte) 1, (byte) 0, 0);
                }
            }
        } catch (final Exception e) {
            if (c.getPlayer() != null) {
                c.getPlayer().dropMessage(1, npc + " has an Error. Please report this");
                System.err.println("Error start executing NPC script, NPC ID : " + npc + " from player: " + c.getPlayer().getName());
            } else {
                System.err.println("Error start executing NPC script, NPC ID : " + npc + ".");
            }
            // FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Error executing NPC script,
            // NPC ID : " + npc + "." + e);
            e.printStackTrace();
            dispose(c);
        }
    }

    public final void start(final MapleClient c, final int npc) {
        if (c == null || GameConstants.getLock()) {
            return;
        }
        try {
            c.setChat(true);
            if (c.canClickNPC()) {
                if (!GameConstants.isBlockedNpc(npc) && c.getPlayer().getBattleMode()) {
                    GameConstants.addNpc(c.getPlayer(), npc);
                    startNPC(c, npc, "pal_battle");
                    return;
                }
                Invocable iv = getInvocable("npc/" + npc + ".js", c);

                if (iv == null) {
                    startNPC(c, npc, "moral_quest");
                    return;
                }
                final NPCConversationManager cm = new NPCConversationManager(c, npc, -1, (byte) -1, iv);
                // System.out.println("CM: " + cm);
                engine.put("cm", cm);
                c.setCMS(cm);
                c.setNpcScript(iv);
                c.getPlayer().setConversation(1);
                c.setClickedNPC();
                // System.out.println("NPCID started: " + npc);
                try {
                    iv.invokeFunction("start"); // Temporary until I've removed all of start
                } catch (NoSuchMethodException nsme) {
                    iv.invokeFunction("action", (byte) 1, (byte) 0, 0);
                }
            }
        } catch (final Exception e) {
            if (c.getPlayer() != null) {
                c.getPlayer().dropMessage(1, npc + " has an Error. Please report this");
                System.err.println("Error start executing NPC script, NPC ID : " + npc + " from player: " + c.getPlayer().getName());
            } else {
                System.err.println("Error start executing NPC script, NPC ID : " + npc + ".");
            }

            // FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Error executing NPC script,
            // NPC ID : " + npc + "." + e);
            e.printStackTrace();
            dispose(c);
        }
    }

    public final void action(final MapleClient c, final byte mode, final byte type, final int selection) {
        if (c == null || GameConstants.getLock()) {
            return;
        }
        if (mode != -1) {
            NPCConversationManager cm = c.getCMS();
            if (cm != null) {
                if (c.getChat()) {
                    npclock = true;
                    //if (cm.getLastMsg() > -1) {
                    //    dispose(c);
                    //    return;
                    //}

                    final Lock lock = c.getNPCLock();
                    lock.lock();
                    try {
                        c.setClickedNPC();
                        cm.getIv().invokeFunction("action", mode, type, selection);
                    } catch (final ScriptException | NoSuchMethodException e) {
                        if (c.getPlayer() != null) {
                            c.getPlayer().dropMessage(1, cm.getNpc() + " has an Error. Please report this");
                            System.err.println("Error executing action NPC script, NPC ID : " + cm.getNpc() + " from player: " + c.getPlayer().getName());
                        } else {
                            System.err.println("Error executing action NPC script, NPC ID : " + cm.getNpc() + ".");
                        }
                        // FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Error executing NPC script,
                        // NPC ID : " + npc + "." + e);
                        e.printStackTrace();
                        dispose(c);
                    } finally {
                        lock.unlock();
                    }
                }
            }
        }
    }

    public void dispose(MapleClient c) {
        if (c == null || c.getPlayer() == null) {
            return;
        }
        NPCConversationManager cm = c.getCMS();
        if (cm != null) {
            try {
                if (cm.getScriptName() != null) {
                    // System.out.println("Script: " + cm.getScriptName());
                    resetContext("npc/" + cm.getScriptName() + ".js", c);
                } else {
                    // System.out.println("Script: " + cm.getNpc());
                    resetContext("npc/" + cm.getNpc() + ".js", c);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        c.setCMS(null);
        c.setNpcScript(null);
        c.getPlayer().setConversation(0);
        c.getPlayer().open(false);
        c.setChat(false);
        c.setNpcCoolDown(System.currentTimeMillis() + 500);
    }

    public final NPCConversationManager getCM(final MapleClient c) {
        return c.getCMS();
    }

    public void startNPC(final MapleClient c, final int npc, String filename) {
        if (c.getCMS() == null) {
            if (filename != null) {
                start(c, npc, filename);
            } else {
                start(c, npc);
            }
        }
    }
}
