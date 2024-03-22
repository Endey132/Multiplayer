package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.combat.*;
import data.scripts.plugins.MPBasePlugin;
import data.scripts.plugins.MPClientPlugin;
import data.scripts.plugins.MPPlugin;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import data.scripts.loading.TempClassLoader;
import data.scripts.plugins.ai.MPDefaultAutofireAIPlugin;
import data.scripts.plugins.ai.MPDefaultMissileAIPlugin;

public class MPModPlugin extends BaseModPlugin {

    public static String VERSION = "v0.1.1";

    private static MPPlugin PLUGIN;

    /**
     * Bypass Starsector's class loader restrictions by loading through a custom class loader
     * Allows use of reflection, java.io, etc. as long as the class was generated through the custom class loader, or within a class that was.
     * Credit to andylizi (Method originates from his Planet Search plugin, found here https://github.com/andylizi/starsector-planet-search)
     * Alex's stance on bypassing class loader https://fractalsoftworks.com/forum/index.php?topic=23229.msg354196
     */
    public static final ClassLoader MPClassLoader;
    static{
        ClassLoader loader;
        try{
            ClassLoader cl = MPModPlugin.class.getClassLoader();
            while (cl != null && !(cl instanceof URLClassLoader)) cl = cl.getParent();
            if(cl == null) throw new RuntimeException("Unable to find URLClassLoader");
            URL[] urls = ((URLClassLoader)cl).getURLs();

            loader = new TempClassLoader(urls, ClassLoader.getSystemClassLoader());
        } catch (RuntimeException | Error ex){
            throw ex;
        } catch (Throwable t){
            throw new ExceptionInInitializerError(t);
        }
        MPClassLoader = loader;
    }

    /**
     * Map for converting between wrappers and primitive types, for use in MPLoadClass
     */
    private final static Map<Class<?>, Class<?>> wPMap = new HashMap<>();
    static {
        wPMap.put(Boolean.class, boolean.class);
        wPMap.put(Byte.class, byte.class);
        wPMap.put(Short.class, short.class);
        wPMap.put(Character.class, char.class);
        wPMap.put(Integer.class, int.class);
        wPMap.put(Long.class, long.class);
        wPMap.put(Float.class, float.class);
        wPMap.put(Double.class, double.class);
    }

    /**
     * Returns the constructor output of a specified class, through the custom class loader
     */
    public static <T> T MPLoadClass(String name, Object[] args){
        try{
            Global.getLogger(MPModPlugin.class).debug("Custom Loading class: " + name);

            if(args != null){
                ArrayList<Class<?>> argTypes = new ArrayList<>();
                ArrayList<Object> argList = new ArrayList<>();
                for(int i = 0; i < args.length; i++){
                    Object arg = args[i];
                    Class<?> argClass = arg.getClass();
                    if(wPMap.containsKey(argClass))
                        argClass = wPMap.get(argClass);
                    argTypes.add(argClass);
                    argList.add(arg);
                }
                return (T)MethodHandles.lookup().findConstructor(Class.forName(name, true, MPClassLoader), MethodType.methodType(void.class, argTypes)).invokeWithArguments(argList);
            }

            return (T) MethodHandles.lookup().findConstructor(Class.forName(name, true, MPClassLoader), MethodType.methodType(void.class)).invoke();
        } catch (RuntimeException | Error ex){
            throw ex;
        } catch (Throwable t){
            throw new ExceptionInInitializerError(t);
        }
    }

    /**
     * Initialize MPBasePlugin & ClassLoaderEditor
     */
    @Override
    public void onApplicationLoad(){
        Script base = MPLoadClass(MPModPlugin.class.getPackage().getName() + ".plugins.MPBasePlugin", null);
        base.run();
        Global.getLogger(MPModPlugin.class).info("Initialized MPBasePlugin");

        MPLoadClass(MPModPlugin.class.getPackage().getName() + ".loading.ClassLoaderEditor", null);
        Global.getLogger(MPBasePlugin.class).info("Initialized ClassLoaderEditor");
    }

    @Override
    public PluginPick<AutofireAIPlugin> pickWeaponAutofireAI(WeaponAPI weapon) {
        if (getPlugin() != null && getPlugin().getType() == MPPlugin.PluginType.CLIENT) {
            MPDefaultAutofireAIPlugin plugin = MPLoadClass(MPModPlugin.class.getPackage().getName() + ".ai.MPDefaultAutofireAIPlugin", new Object[]{weapon});

            ShipAPI ship = weapon.getShip();
            MPClientPlugin clientPlugin = (MPClientPlugin) getPlugin();
            Map<String, MPDefaultAutofireAIPlugin> plugins = clientPlugin.getShipTable().getTempAutofirePlugins().get(ship.getId());
            if (plugins == null) plugins = new HashMap<>();
            plugins.put(weapon.getSlot().getId(), plugin);
            clientPlugin.getShipTable().getTempAutofirePlugins().put(ship.getId(), plugins);

            return new PluginPick<>((AutofireAIPlugin) plugin, CampaignPlugin.PickPriority.HIGHEST);
        }
        return null;
    }

    @Override
    public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        if (getPlugin() != null && getPlugin().getType() == MPPlugin.PluginType.CLIENT) {
            MPDefaultMissileAIPlugin plugin = MPLoadClass(MPModPlugin.class.getPackage().getName() + ".ai.MPDefaultMissileAIPlugin", null);

            return new PluginPick<>((MissileAIPlugin) plugin, CampaignPlugin.PickPriority.HIGHEST);
        }
        return null;
    }

    public static void setPlugin(MPPlugin plugin) {
        if (PLUGIN != null) {
            Global.getCombatEngine().removePlugin(PLUGIN);
        }
        Global.getCombatEngine().addPlugin(plugin);
        PLUGIN = plugin;
    }

    public static void destroyPlugin() {
        if (PLUGIN == null) return;
        Global.getCombatEngine().removePlugin(PLUGIN);
    }

    public static MPPlugin getPlugin() {
        return PLUGIN;
    }
}