package data.scripts.plugins;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import data.scripts.MPModPlugin;


/**
 * Loads MPUIPlugin through custom class loader
 */
public class MPUIPluginSetup extends BaseEveryFrameCombatPlugin {
    @Override
    public void init(CombatEngineAPI engine){
        try{
            EveryFrameCombatPlugin script = MPModPlugin.MPLoadClass(MPModPlugin.class.getPackage().getName() + ".plugins.gui.MPUIPlugin", null);
            engine.addPlugin(script);
        } catch (RuntimeException | Error e){
            throw e;
        } catch (Throwable t){
            throw new AssertionError("unreachable", t);
        }
    }
}
