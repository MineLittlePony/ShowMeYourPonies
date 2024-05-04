package com.minelittlepony.smyp;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.render.*;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import nl.enjarai.showmeyourskin.client.ModRenderLayers;
import nl.enjarai.showmeyourskin.config.HideableEquipment;
import nl.enjarai.showmeyourskin.util.ArmorContext;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.client.model.armour.ArmourLayer;
import com.minelittlepony.client.model.armour.ArmourRendererPlugin;

public class Main implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ArmourRendererPlugin.register(PluginImpl::new);
    }

    static final class PluginImpl implements ArmourRendererPlugin {
        @Nullable
        private ArmorContext context;

        private final ArmourRendererPlugin parent;

        PluginImpl(ArmourRendererPlugin parent) {
            this.parent = parent;
        }

        @Override
        public ItemStack[] getArmorStacks(LivingEntity entity, EquipmentSlot armorSlot, ArmourLayer layer) {
            context = new ArmorContext(HideableEquipment.fromSlot(armorSlot), entity);
            return parent.getArmorStacks(entity, armorSlot, layer);
        }

        @Override
        public float getGlintAlpha(EquipmentSlot slot, ItemStack stack) {
            return parent.getGlintAlpha(slot, stack) * (context == null || !context.shouldModify() ? 1 : context.getApplicableGlintTransparency());
        }

        @Override
        public float getArmourAlpha(EquipmentSlot slot, ArmourLayer layer) {
            return context != null && context.shouldModify() ? context.getApplicablePieceTransparency() : parent.getArmourAlpha(slot, layer);
        }

        @Override
        public float getTrimAlpha(EquipmentSlot slot, RegistryEntry<ArmorMaterial> material, ArmorTrim trim, ArmourLayer layer) {
            return context != null && context.shouldModify() ? context.getApplicableTrimTransparency() : parent.getTrimAlpha(slot, material, trim, layer);
        }

        @Override
        @Nullable
        public RenderLayer getArmourLayer(EquipmentSlot slot, Identifier texture, ArmourLayer layer) {
            return context != null && context.shouldModify() && context.getApplicablePieceTransparency() < 1
                    ? ModRenderLayers.ARMOR_TRANSLUCENT_NO_CULL.apply(texture)
                    : parent.getArmourLayer(slot, texture, layer);
        }

        @Override
        @Nullable
        public RenderLayer getTrimLayer(EquipmentSlot slot, RegistryEntry<ArmorMaterial> material, ArmorTrim trim, ArmourLayer layer) {
            return context != null && context.shouldModify() && context.getApplicableTrimTransparency() < 1
                    ? ModRenderLayers.ARMOR_TRANSLUCENT_NO_CULL.apply(TexturedRenderLayers.ARMOR_TRIMS_ATLAS_TEXTURE)
                    : parent.getTrimLayer(slot, material, trim, layer);

        }
    }
}
