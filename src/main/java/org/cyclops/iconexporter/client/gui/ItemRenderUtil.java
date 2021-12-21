package org.cyclops.iconexporter.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.helper.FluidHelpers;
import org.cyclops.cyclopscore.helper.GuiHelpers;

import javax.annotation.Nullable;

/**
 * Utilities for rendering items.
 * @author rubensworks
 */
public class ItemRenderUtil {

    public static void renderItem(ItemStack itemStack, float scale) {
        // Based on Integrated Dynamics's ItemValueTypeWorldRenderer
        RenderSystem.scalef(scale, scale, scale);
        RenderSystem.pushMatrix();
        RenderSystem.scaled(0.0625, 0.0625, 0.01);
        ItemLightingUtil.enableGUIStandardItemLighting(scale);

        RenderSystem.pushMatrix();
        RenderSystem.rotatef(40f, 0.0F, 1.0F, 0.0F);
        RenderSystem.rotatef(95F, 1.0F, 0.0F, 0.0F);
        RenderSystem.popMatrix();

        RenderSystem.enablePolygonOffset();
        RenderSystem.polygonOffset(-1, -1);

        RenderSystem.pushTextureAttributes();
        RenderSystem.enableRescaleNormal();
        RenderSystem.popAttributes();

        renderItemAndEffectIntoGUI(itemStack, 0, 0, scale);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();

        RenderSystem.disablePolygonOffset();

        RenderSystem.popMatrix();
        RenderHelper.turnOff();
    }

    public static void renderFluid(AbstractGui gui, MatrixStack matrixStack, Fluid fluid, float scale) {
        GlStateManager._scaled(scale / 16, scale / 16, scale / 16);
        ItemLightingUtil.enableGUIStandardItemLighting(scale);
        GuiHelpers.renderFluidSlot(gui, matrixStack, new FluidStack(fluid, FluidHelpers.BUCKET_VOLUME), 0, 0);
    }

    // ----- Everything below is modified from RenderItem -----

    public static void renderItemAndEffectIntoGUI(ItemStack stack, int xPosition, int yPosition, float scale) {
        renderItemAndEffectIntoGUI(Minecraft.getInstance().player, stack, xPosition, yPosition, scale);
    }

    public static void renderItemAndEffectIntoGUI(@Nullable LivingEntity entityIn, ItemStack itemIn, int x, int y, float scale) {
        if (!itemIn.isEmpty()) {
            Minecraft.getInstance().getItemRenderer().blitOffset += 50.0F;

            try {
                renderItemModelIntoGUI(itemIn, x, y, Minecraft.getInstance().getItemRenderer().getModel(itemIn, (World)null, entityIn), scale);
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering item");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Item being rendered");
                crashreportcategory.setDetail("Item Type", () -> {
                    return String.valueOf((Object)itemIn.getItem());
                });
                crashreportcategory.setDetail("Registry Name", () -> String.valueOf(itemIn.getItem().getRegistryName()));
                crashreportcategory.setDetail("Item Damage", () -> {
                    return String.valueOf(itemIn.getDamageValue());
                });
                crashreportcategory.setDetail("Item NBT", () -> {
                    return String.valueOf((Object)itemIn.getTag());
                });
                crashreportcategory.setDetail("Item Foil", () -> {
                    return String.valueOf(itemIn.hasFoil());
                });
                throw new ReportedException(crashreport);
            }

            Minecraft.getInstance().getItemRenderer().blitOffset -= 50.0F;
        }
    }

    protected static void renderItemModelIntoGUI(ItemStack stack, int x, int y, IBakedModel bakedmodel, float scale) {
        RenderSystem.pushMatrix();
        Minecraft.getInstance().getTextureManager().bind(AtlasTexture.LOCATION_BLOCKS);
        Minecraft.getInstance().getTextureManager().getTexture(AtlasTexture.LOCATION_BLOCKS).setFilter(false, false);
        RenderSystem.enableRescaleNormal();
        RenderSystem.enableAlphaTest();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.translatef((float)x, (float)y, 100.0F + Minecraft.getInstance().getItemRenderer().blitOffset);
        RenderSystem.translatef(8.0F, 8.0F, 0.0F);
        RenderSystem.scalef(1.0F, -1.0F, 1.0F);
        RenderSystem.scalef(16.0F, 16.0F, 16.0F);
        MatrixStack matrixstack = new MatrixStack();
        IRenderTypeBuffer.Impl irendertypebuffer$impl = Minecraft.getInstance().renderBuffers().bufferSource();
        boolean flag = !bakedmodel.usesBlockLight();
        if (flag) {
            ItemLightingUtil.setupGuiFlatDiffuseLighting(scale);
        }

        Minecraft.getInstance().getItemRenderer().render(stack, ItemCameraTransforms.TransformType.GUI, false, matrixstack, irendertypebuffer$impl, 15728880, OverlayTexture.NO_OVERLAY, bakedmodel);
        irendertypebuffer$impl.endBatch();
        RenderSystem.enableDepthTest();
        if (flag) {
            RenderHelper.setupFor3DItems();
        }

        RenderSystem.disableAlphaTest();
        RenderSystem.disableRescaleNormal();
        RenderSystem.popMatrix();
    }

}
