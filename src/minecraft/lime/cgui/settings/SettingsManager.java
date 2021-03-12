package lime.cgui.settings;

import lime.Lime;
import lime.module.Module;

import java.util.ArrayList;
public class SettingsManager {

    private ArrayList<Setting> settings;

    public SettingsManager(){
        this.settings = new ArrayList<Setting>();
    }

    public void rSetting(Setting in){
        this.settings.add(in);
    }

    public ArrayList<Setting> getSettings(){
        return this.settings;
    }

    public ArrayList<Setting> getSettingsByMod(Module mod){
        ArrayList<Setting> out = new ArrayList<Setting>();
        for(Setting s : getSettings()){
            if(s.getParentMod().equals(mod)){
                out.add(s);
            }
        }
        if(out.isEmpty()){
            return null;
        }
        return out;
    }

    public Setting getSettingByName(String name){
        for(Setting set : getSettings()){
            if(set.getName().equalsIgnoreCase(name)){
                return set;
            }
        }
        System.err.println("["+ Lime.clientName + "] Error Setting NOT found: '" + name +"'!");
        return null;
    }
    public Setting getSettingByNameAndMod(String name, Module mod){
        for(Setting set : getSettings()){
            if(set.getName().equalsIgnoreCase(name) && set.getParentMod() == mod){
                return set;
            }
        }
        //System.err.println("["+ Lime.clientName + "] Error Setting NOT found: '" + name +"'!");
        return null;
    }

}