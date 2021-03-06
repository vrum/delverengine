package com.interrupt.dungeoneer.entities.triggers;

import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;

public class BasicTrigger extends Entity {
    @EditorProperty( group = "Trigger" )
    public boolean isSecret = false;

    public enum TriggerStatus {WAITING, TRIGGERED, RESETTING, DESTROYED}
    public enum GameTime {WHENEVER, DESCENT, ESCAPE}

    @EditorProperty( group = "Trigger" )
    public String triggersId = "";

    @EditorProperty( group = "Trigger" )
    public boolean triggerResets = true;

    @EditorProperty( group = "Trigger" )
    public float triggerDelay = 0f;

    @EditorProperty( group = "Trigger" )
    public float triggerResetTime = 0f;

    @EditorProperty( group = "Trigger" )
    public String triggerValue = "";

    @EditorProperty( group = "Trigger" )
    public boolean triggerPropogates = true;

    @EditorProperty( group = "Trigger" )
    public String message = "";

    @EditorProperty( group = "Trigger" )
    public float messageTime = 5f;

    @EditorProperty( group = "Trigger" )
    public float messageSize = 1f;

    @EditorProperty( group = "Trigger" )
    public String triggerSound = null;

    @EditorProperty( group = "Trigger" )
    public Trigger.GameTime triggersDuring = Trigger.GameTime.WHENEVER;

    @EditorProperty( group = "End Game" )
    public boolean appearsDuringEndgame = true;

    protected boolean selfDestructs = true;

    protected Trigger.TriggerStatus triggerStatus= Trigger.TriggerStatus.WAITING;
    private float triggerTime = 0;

    public BasicTrigger() {
        hidden = true; spriteAtlas = "editor"; tex = 11; floating = true;
    }

    @Override
    public void init(Level level, Level.Source source) {
        if(Game.instance != null && Game.instance.player != null) {
            // Might need to despawn this during the endgame
            if(Game.instance.player.isHoldingOrb) {
                if(!appearsDuringEndgame) {
                    isActive = false;
                }
            }
        }
    }

    @Override
    public void tick(Level level, float delta) {
        if (triggerStatus== Trigger.TriggerStatus.DESTROYED && selfDestructs){
            this.isActive=false;
        }
        if (triggerStatus== Trigger.TriggerStatus.RESETTING){
            triggerTime-=delta;
            if (triggerTime<=0){
                triggerStatus= Trigger.TriggerStatus.WAITING;
                triggerTime=triggerDelay;
            }
        }
        if (triggerStatus== Trigger.TriggerStatus.TRIGGERED){
            triggerTime-=delta;
            if (triggerTime<=0){
                doTriggerEvent(triggerValue); // fire!
                if (triggerResets){
                    triggerStatus= Trigger.TriggerStatus.RESETTING;
                    triggerTime=triggerResetTime;
                } else {
                    triggerStatus= Trigger.TriggerStatus.DESTROYED;
                }
            }
        }
    }

    @Override
    public void use(Player p, float projx, float projy) {
        fire(null);
    }

    public void fire(String value) {

        // Check if we can actually fire now
        if(triggersDuring != Trigger.GameTime.WHENEVER) {
            if(Game.instance != null && Game.instance.player != null) {
                boolean endgame = Game.instance.player.isHoldingOrb;
                if(triggersDuring == Trigger.GameTime.DESCENT && endgame) {
                    return;
                }
                else if(triggersDuring == Trigger.GameTime.ESCAPE && !endgame) {
                    return;
                }
            }
        }

        // Track secrets
        if(isSecret) {
            isSecret = false;
            Game.instance.player.history.foundSecret();
        }

        // Triggering an already triggered trigger will do nothing
        if (triggerStatus== Trigger.TriggerStatus.WAITING){
            triggerStatus= Trigger.TriggerStatus.TRIGGERED;
            triggerTime=triggerDelay;

            // update the value if one was given
            if(value != null && !value.equals(""))
                triggerValue=value;
        }
    }

    @Override
    public void onTrigger(Entity instigator, String value) {
        if(triggerPropogates) {
            fire(value);
        }
        else {
            // just update the value if one was given
            if(value != null && !value.equals(""))
                triggerValue=value;
        }
    }

    // triggers can be delayed, fire the actual trigger here
    public void doTriggerEvent(String value) {
        Audio.playPositionedSound(triggerSound, new Vector3((float)x,(float)y,(float)z), 0.8f, 11f);
        Game.instance.level.trigger(this, triggersId, triggerValue);
        if(message != null && !message.equals("")) Game.ShowMessage(message, messageTime, messageSize);
    }

    @Override
    public void makeEntityIdUnique(String idPrefix) {
        super.makeEntityIdUnique(idPrefix);
        triggersId = makeUniqueIdentifier(triggersId, idPrefix);
    }

    public Trigger.TriggerStatus getTriggerStatus() {
        return triggerStatus;
    }
}
