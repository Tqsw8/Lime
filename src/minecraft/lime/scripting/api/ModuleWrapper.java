package lime.scripting.api;

import jdk.nashorn.api.scripting.AbstractJSObject;
import lime.core.Lime;
import lime.features.setting.impl.BoolValue;
import lime.features.setting.impl.ColorValue;
import lime.features.setting.impl.SlideValue;
import lime.features.setting.impl.TextValue;
import lime.scripting.ScriptModule;
public class ModuleWrapper extends AbstractJSObject {
    private final ScriptModule module;

    public ModuleWrapper(ScriptModule module)
    {
        this.module = module;
    }

    public ScriptModule getModule() {
        return module;
    }


    @Override
    public Object getMember(String name) {
        if(name.equals("getName")) {
            return new AbstractJSObject() {
                @Override
                public Object call(Object thiz, Object... args) {
                    return module.getName();
                }
            };
        }
        if(name.equals("registerEvent")) {
            return new AbstractJSObject() {
                @Override
                public Object call(Object thiz, Object... args) {
                    if(!getModule().getEvents().containsKey((String) args[0])) {
                        getModule().getEvents().put((String) args[0], (AbstractJSObject) args[1]);
                    }
                    return null;
                }
            };
        }
        if(name.equals("registerSlider")) {
            return new AbstractJSObject() {
                @Override
                public Object call(Object thiz, Object... args) {
                    new SlideValue((String) args[0], module, Double.parseDouble(args[1] + ""), Double.parseDouble(args[2] + ""), Double.parseDouble(args[3] + ""), Double.parseDouble(args[4] + ""));
                    return null;
                }
            };
        }
        if(name.equals("registerBoolean")) {
            return new AbstractJSObject() {
                @Override
                public Object call(Object thiz, Object... args) {
                    new BoolValue((String) args[0], module, Boolean.parseBoolean(args[1] + ""));
                    return null;
                }
            };
        }
        if(name.equals("registerText")) {
            return new AbstractJSObject() {
                @Override
                public Object call(Object thiz, Object... args) {
                    new TextValue((String) args[0], module, (String) args[1]);
                    return null;
                }
            };
        }
        if(name.equals("registerColorPicker")) {
            return new AbstractJSObject() {
                @Override
                public Object call(Object thiz, Object... args) {
                    new ColorValue((String) args[0], module, (Integer) args[1]);
                    return null;
                }
            };
        }
        if(name.equals("getBoolean")) {
            return new AbstractJSObject() {
                @Override
                public Object call(Object thiz, Object... args) {
                    return ((BoolValue) Lime.getInstance().getSettingsManager().getSetting((String) args[0], module)).isEnabled();
                }
            };
        }
        if(name.equals("getSlider")) {
            return new AbstractJSObject() {
                @Override
                public Object call(Object thiz, Object... args) {
                    return ((SlideValue) Lime.getInstance().getSettingsManager().getSetting((String) args[0], module)).getCurrent();
                }
            };
        }
        if(name.equals("getText")) {
            return new AbstractJSObject() {
                @Override
                public Object call(Object thiz, Object... args) {
                    return ((TextValue) Lime.getInstance().getSettingsManager().getSetting((String) args[0], module)).getText();
                }
            };
        }
        if(name.equals("getColorPicker")) {
            return new AbstractJSObject() {
                @Override
                public Object call(Object thiz, Object... args) {
                    return ((ColorValue) Lime.getInstance().getSettingsManager().getSetting((String) args[0], module)).getColor();
                }
            };
        }
        return super.getMember(name);
    }
}
