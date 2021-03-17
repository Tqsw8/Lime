package lime.cgui;

import lime.Lime;
import lime.cgui.component.Component;
import lime.cgui.component.button.*;
import lime.cgui.component.button.Checkbox;
import lime.settings.Setting;
import lime.module.Module;
import lime.utils.Timer;
import lime.utils.render.Util2D;
import lime.utils.render.animations.AnimationSlideSmooth;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

import static lime.module.impl.render.OldHUD.fix;

public class ClickGui extends GuiScreen {
    public int x = 0;
    public int y = 0;
    public int width = 250;
    public int height = 300;
    public int offset = 0;
    public int offsetSet = 0;
    public int alpha = 255;
    public Timer timer = new Timer();
    public AnimationSlideSmooth animationSlideSmooth = new AnimationSlideSmooth();
    public int xDrag, yDrag;
    public boolean drag;
    public Module.Category currentCat;
    public Module currentMod;
    public int rendered;
    public ArrayList<Component> components = new ArrayList<Component>();
    public Module modBinding;
    private boolean wasClosed = true;
    ArrayList<Module> modules;
    public ClickGui(ArrayList<Module> modules){
        this.modules = modules;
    }

    @Override
    public void onGuiClosed() {
        if (mc.entityRenderer.theShaderGroup != null) {
            mc.entityRenderer.theShaderGroup.deleteShaderGroup();
            mc.entityRenderer.theShaderGroup = null;
        }
        wasClosed = true;
        components.clear();
        super.onGuiClosed();
    }

    @Override
    public void initGui() {
        alpha = 255;
        animationSlideSmooth.setValue(0);
        if(!mc.gameSettings.ofFastRender &&  mc.getRenderViewEntity() instanceof EntityPlayer && OpenGlHelper.shadersSupported && Lime.setmgr.getSettingByName("Blur").getValBoolean()){
            if (mc.entityRenderer.theShaderGroup != null) {
                mc.entityRenderer.theShaderGroup.deleteShaderGroup();
            }
            mc.entityRenderer.loadShader(new ResourceLocation("shaders/post/blur.json"));
        }
        if(wasClosed){
            ArrayList<Setting> sets = new ArrayList<>();
            for(Setting set: Lime.setmgr.getSettings()){
                if(set.isCheck() && set.parentSet != null)
                    sets.add(set);
            }
            for(Setting set: Lime.setmgr.getSettings()){
                if(set.isCombo())
                    components.add(new Combo(set));
                if(set.isComboBool()){
                    ArrayList<Setting> toSend = new ArrayList<>();
                    for(Setting set2 : sets){
                        if(set == set2.parentSet){
                            toSend.add(set2);
                        }
                    }
                    Setting[] toSendArray = new Setting[toSend.size()];
                    toSendArray = toSend.toArray(toSendArray);
                    components.add(new ComboBoolean(set, toSendArray));
                }
                if(set.isSlider())
                    components.add(new Slider(set));
                if(set.isCheck() && set.parentSet == null)
                    components.add(new Checkbox(set));
                if(set.isColor())
                    components.add(new ColorPicker(set));
            }
            wasClosed =false;
        }
        drag = false;
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GL11.glPushMatrix();
        GlStateManager.translate(0, animationSlideSmooth.calculate(-new ScaledResolution(this.mc).getScaledHeight(), 0), 0);
        if(drag) {
            x = xDrag + mouseX;
            y = yDrag + mouseY;
        }
        scroll(mouseX, mouseY);
        scrollSet(mouseX, mouseY);
        drawPanel(x, y, mouseX, mouseY);
        GL11.glPopMatrix();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    public void scrollSet(int mouseX, int mouseY){
        if(hover(x + width / 2, y + 55, mouseX, mouseY, width / 2, height - 55)){
            if(Mouse.hasWheel()){
                int maxOffsetCanBe = 0;
                for(Component component : components){
                    if(component.set.getParentMod() != currentMod) continue;
                    if(component.set.isCombo()) maxOffsetCanBe += 16;
                    if(component.set.isSlider()) maxOffsetCanBe += 18;
                    if(component.set.isCheck()) maxOffsetCanBe += 14;
                    if(component.set.isComboBool()) maxOffsetCanBe += 16;
                    if(component.set.isColor()) maxOffsetCanBe += 86;
                }
                maxOffsetCanBe -= height - 55;

                int wheel = Mouse.getDWheel();
                if(wheel < 0 && maxOffsetCanBe + 10 > offsetSet){
                    offsetSet += 10;
                    components.stream().filter(component -> component.set.isColor() && component.set.getParentMod() == currentMod).forEach(component -> component.set.posYColor = component.set.posYColor - 10);
                } else if(wheel > 0 && offsetSet - 10 >= 0){
                    offsetSet -= 10;
                    components.stream().filter(component -> component.set.isColor() && component.set.getParentMod() == currentMod).forEach(component -> component.set.posYColor = component.set.posYColor + 10);
                }
            }
        }
    }

    public void scroll(int mouseX, int mouseY){
        if(hover(x, y + 55, mouseX, mouseY, width / 2, height - 55)){
            ArrayList<Module> mods = Lime.moduleManager.getModulesByCategory(currentCat);
            int maxOffset = -1;
            if(mods.size() > 8){
                maxOffset = (mods.size() - 8) * 30;
            }
            if(Mouse.hasWheel() && maxOffset != -1){
                int wheel = Mouse.getDWheel();
                if(wheel < 0 && offset + 9 < maxOffset){
                    offset += 10;
                } else if(wheel > 0 && offset > 0) {
                    offset -= 10;
                }
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    void drawPanel(int xPos, int yPos, int mouseX, int mouseY) {
        rendered = 0;
        Util2D.drawRoundedRect(xPos - 1, yPos - 1, xPos + width + 1, yPos + height + 1, new Color(50, 50, 50, 255).getRGB(), new Color(50, 50, 50, alpha).getRGB());
        Util2D.drawRoundedRect(xPos, yPos, xPos + width, yPos + height, new Color(25, 25, 25, alpha).getRGB(), new Color(25, 25, 25, 255).getRGB());
        Lime.fontManager.comfortaa_hud.drawString(Lime.clientName, x + (width / 2) - Lime.fontManager.comfortaa_hud.getStringWidth(Lime.clientName) + 10, y + 4, new Color(255, 255, 255, alpha).getRGB());
        Gui.drawRect(xPos, y + 14, x + width, yPos + 15, new Color(50, 50, 50, alpha).getRGB());
        Gui.drawRect(xPos, yPos + 54, x + width, yPos + 55, new Color(50, 50, 50, alpha).getRGB());
        Gui.drawRect(xPos + (width / 2), yPos + 54, xPos + (width / 2) + 1, y + height, new Color(50, 50, 50, alpha).getRGB());
        Util2D.DrawCroix(x + width - 15, y +1, 10, new Color(50, 50, 50, alpha).getRGB());
        int i = 0;
        for(Module.Category c : Module.Category.values()){
            if(!c.name().equalsIgnoreCase("misc")){
                Gui.drawRect(0, 0, 0, 0, 0);
                boolean flag = hover(x + (i * 50), y + 15, mouseX, mouseY, 50, 40);
                if(flag){
                    Gui.drawRect(x + (i * 50) , y + 15, x + (i * 50) + 10 + 40, y + 19 + 35, new Color(75, 75, 75, 255).getRGB());
                }
                Util2D.drawImage(new ResourceLocation("textures/icons/" + c.name().toLowerCase() + ".png"), x + (i * 50) + 10, y + 19, 32, 32);
                Gui.drawRect(x + (i * 50) + 50, y + 15, x + (i * 50) + 51, y + 55, new Color(50, 50, 50).getRGB());
            } else {
                boolean flag = hover(x + (i * 50) + 10, y + 19, mouseX, mouseY, 32, 32);
                if(flag)
                    Gui.drawRect(x + (i * 50) , y + 15, x + (i * 50) + 10 + 40, y + 54, new Color(75, 75, 75).getRGB());
                Util2D.drawImage(new ResourceLocation("textures/icons/misc.png"), x + 200 + 10, y + 19, 32, 32);
            }
            i++;
        }
        GL11.glPushMatrix();
        GL11.glEnable(3089);
        prepareScissorBox(x, y + 56, x + 128, y + 300);
        for(Module m : modules){
            if(m.getCat() != currentCat) continue;
            boolean flag = hover(x, yPos - offset - 80 + (i * 30) - 14, mouseX, mouseY, width / 2, 27);
            if(flag){
                Gui.drawRect(x, yPos - offset - 80 + (i * 30) + 15, x + width / 2, yPos - offset - 80 + (i * 30) - 15, new Color(75, 75, 75, alpha).getRGB());
            }
            Lime.fontManager.comfortaa_hud.drawString(m.binding ? "Set a key" : m.name, x + 5, yPos - offset - 80 + (i * 30) - 2, new Color(255, 255, 255, alpha).getRGB());
            if(m.binding)
                Util2D.drawImage(new ResourceLocation("textures/icons/keybind.png"), x + (width / 2) - 25, yPos - offset - 80 + (i * 30) - 4, 10, 10);
            else
                Util2D.drawFullCircle(x + (width / 2) - 20, yPos - offset - 80 + (i * 30) + 1, 5, 5, m.toggled ? new Color(0, 150, 0, alpha).getRGB() : new Color(75, 0, 0, alpha).getRGB());
            fix();
            Gui.drawRect(x, yPos - offset  - 80 + (i * 30) + 15, x + (width / 2), yPos - offset - 80 + (i * 30) + 16, new Color(50, 50, 50, alpha).getRGB());
            i++;
        }
        GL11.glDisable(3089);
        GL11.glPopMatrix();
        if(currentCat != null && currentMod != null && currentMod.hasSettings()){
            for(Component component : components){
                if(component.set.getParentMod() == currentMod){
                    GlStateManager.color(1, 1, 1);
                    GL11.glPushMatrix();
                    GL11.glEnable(3089);
                    prepareScissorBox(x + width / 2, y + 56, x + ((width / 2) * 2), y + height);
                    component.render(x, y - offsetSet, width, height, mouseX, mouseY);
                    GL11.glDisable(3089);
                    GL11.glPopMatrix();
                    if(component.set.isCombo()) rendered += 16;
                    if(component.set.isSlider()) rendered += 18;
                    if(component.set.isCheck()) rendered += 14;
                    if(component.set.isComboBool()) rendered += 16;
                    if(component.set.isColor()) rendered += 86;
                    component.rendered = this.rendered - 15;
                }
            }
        }
    }
    public void prepareScissorBox(float x2, float y2, float x22, float y22) {
        ScaledResolution scale = new ScaledResolution(this.mc);
        int factor = scale.getScaleFactor();
        GL11.glScissor((int)(x2 * (float)factor), (int)(((float)scale.getScaledHeight() - y22) * (float)factor), (int)((x22 - x2) * (float)factor), (int)((y22 - y2) * (float)factor));
    }


    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if(hover(x + width - 15, y +1, mouseX, mouseY, 10, 10) && mouseButton == 0) {
            this.mc.displayGuiScreen((GuiScreen)null);

            if (this.mc.currentScreen == null)
            {
                this.mc.setIngameFocus();
            }
        }
        if(hover(x, y, mouseX, mouseY, width, 15) && mouseButton == 0) {
            xDrag = x - mouseX;
            yDrag = y - mouseY;
            drag = true;
        } else {
            drag = false;
        }
        if(hover(x, y + 15, mouseX, mouseY, 50, 40) && mouseButton == 0){
            offset = 0;
            if(currentCat != Module.Category.COMBAT){
                currentMod = null;
            }
            currentCat = Module.Category.COMBAT;
        }
        if(hover(x + 50, y + 15, mouseX, mouseY, 50, 40) && mouseButton == 0){
            offset = 0;
            if(currentCat != Module.Category.MOVEMENT){
                currentMod = null;
            }
            currentCat = Module.Category.MOVEMENT;
        }
        if(hover(x + (2 * 50), y + 15, mouseX, mouseY, 50, 40) && mouseButton == 0){
            offset = 0;
            if(currentCat != Module.Category.PLAYER){
                currentMod = null;
            }
            currentCat = Module.Category.PLAYER;
        }
        if(hover(x + (3 * 50), y + 15, mouseX, mouseY, 50, 40) && mouseButton == 0){
            offset = 0;
            if(currentCat != Module.Category.RENDER){
                currentMod = null;
            }
            currentCat = Module.Category.RENDER;
        }
        if(hover(x + (4 * 50), y + 15, mouseX, mouseY, 50, 40) && mouseButton == 0){
            offset = 0;
            if(currentCat != Module.Category.MISC){
                currentMod = null;
            }
            currentCat = Module.Category.MISC;
        }
        int i = 5;
        for(Module ignored : modules){
            if(ignored.getCat() != currentCat) continue;
            boolean flag = hover(x, y - offset - 80 + (i * 30) - 14, mouseX, mouseY, width / 2, 27);
            if(flag){
                ArrayList<Module> moduleArrayList = new ArrayList<>();
                for(Module m : modules){
                    if(m.getCat() == currentCat) moduleArrayList.add(m);
                }
                Module mod = moduleArrayList.get(i - 5);
                if(mouseButton == 1) if(mod.hasSettings()) {
                    currentMod = mod;
                    offsetSet = 0;
                }
                if(mouseButton == 2){
                    mod.binding = true;
                    modBinding = mod;
                }
                if(mouseButton == 0) mod.toggle();
            }

            i++;
        }
        if(currentCat != null && currentMod != null && currentMod.hasSettings()){
            for(Component component : components){
                if(component.set.getParentMod() == currentMod)
                    component.mouseClicked(mouseX, mouseY, mouseButton);
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        drag = false;
        if(currentCat != null && currentMod != null && currentMod.hasSettings()){
            for(Component component : components){
                if(component.set.getParentMod() == currentMod)
                    component.mouseReleased(mouseX, mouseY, state);
            }
        }

        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        boolean dontEscape = false;
        if(this.modBinding != null && this.modBinding.binding){
            if(keyCode == 1) {
                dontEscape = true;
                this.modBinding.binding = false;
                this.modBinding = null;
            } else {
                this.modBinding.setKey(keyCode);
                this.modBinding.binding = false;
                this.modBinding = null;
            }

        }
        if (keyCode == 1 && !dontEscape)
        {
            this.mc.displayGuiScreen(null);
            if (this.mc.currentScreen == null)
                this.mc.setIngameFocus();
        }
    }

    public boolean hover(int x, int y, int mouseX, int mouseY, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

}
